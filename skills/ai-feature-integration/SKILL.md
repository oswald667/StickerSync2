---
name: ai-feature-integration
description: >
  Intégration de fonctionnalités IA dans une application React + Django.
  Utiliser ce skill pour : intégrer l'API Claude (Anthropic) dans Django, implémenter
  le streaming de réponses IA vers React, mettre en place un RAG (Retrieval Augmented
  Generation) avec PostgreSQL et pgvector, gérer les embeddings, concevoir l'UX des
  interfaces IA (chat, assistant, suggestions), gérer les coûts et limites de l'API.
  Déclencher sur : "Claude API", "Anthropic", "IA dans mon app", "streaming IA",
  "RAG", "pgvector", "embeddings", "chat IA", "assistant IA", "LLM", "intégration IA",
  "Server-Sent Events", "tokens", "context window", "prompt engineering".
---

# AI Feature Integration

Intégration IA production-grade (Claude API) dans une stack React + Django.

---

## 1. Architecture générale

```
Utilisateur (React)
      │
      │  POST /api/v1/ai/chat/     (message utilisateur)
      │  GET  /api/v1/ai/stream/   (SSE — streaming de la réponse)
      ▼
Django View
      │
      ├── Récupération du contexte (historique, RAG si activé)
      ├── Construction du prompt (system + messages)
      │
      ▼
Anthropic API (Claude)
      │
      ▼
Django Stream → SSE → React (affichage token par token)
      │
      ▼
Sauvegarde en DB (messages, usage tokens)
```

---

## 2. Backend Django — Intégration Claude API

### 2.1 Installation et configuration

```python
# requirements/base.txt
anthropic==0.x

# config/settings/base.py
ANTHROPIC_API_KEY = config("ANTHROPIC_API_KEY")
ANTHROPIC_MODEL = "claude-sonnet-4-20250514"   # Modèle recommandé
ANTHROPIC_MAX_TOKENS = 1024
ANTHROPIC_MAX_CONTEXT_MESSAGES = 20            # Nb messages max dans le contexte
```

### 2.2 Modèles — Stocker les conversations

```python
# apps/ai/models.py
from apps.core.models import BaseModel

class Conversation(BaseModel):
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name="conversations")
    title = models.CharField(max_length=200, blank=True)
    system_prompt = models.TextField(blank=True)   # Prompt système custom par conversation
    total_input_tokens = models.IntegerField(default=0)
    total_output_tokens = models.IntegerField(default=0)

    @property
    def total_cost_usd(self):
        # Claude Sonnet : $3/M input, $15/M output (vérifier les tarifs actuels)
        return (self.total_input_tokens * 3 + self.total_output_tokens * 15) / 1_000_000

class Message(BaseModel):
    conversation = models.ForeignKey(Conversation, on_delete=models.CASCADE, related_name="messages")
    role = models.CharField(max_length=20, choices=[("user", "user"), ("assistant", "assistant")])
    content = models.TextField()
    input_tokens = models.IntegerField(default=0)
    output_tokens = models.IntegerField(default=0)
    latency_ms = models.IntegerField(default=0)   # Temps de réponse pour monitoring
```

### 2.3 Service Claude — Logique métier IA

```python
# apps/ai/services.py
import anthropic
import time
from django.conf import settings

class ClaudeService:
    def __init__(self):
        self.client = anthropic.Anthropic(api_key=settings.ANTHROPIC_API_KEY)

    def _build_messages(self, conversation: "Conversation") -> list:
        """Construire l'historique de messages pour l'API."""
        messages = conversation.messages.order_by("created_at")
        # Limiter à N derniers messages pour respecter la context window
        recent = list(messages)[-settings.ANTHROPIC_MAX_CONTEXT_MESSAGES:]
        return [{"role": m.role, "content": m.content} for m in recent]

    def _get_system_prompt(self, conversation: "Conversation", context: str = "") -> str:
        base_system = conversation.system_prompt or """
        Tu es un assistant helpful, précis et concis.
        Tu réponds en français sauf si l'utilisateur écrit dans une autre langue.
        Tu es direct et évites les introductions superflues.
        """
        if context:
            return f"{base_system}\n\nContexte pertinent pour cette question :\n{context}"
        return base_system

    def stream_response(self, conversation, new_user_message: str, rag_context: str = ""):
        """Générateur de streaming — yielde les tokens au fur et à mesure."""
        # Sauvegarder le message utilisateur
        user_msg = Message.objects.create(
            conversation=conversation,
            role="user",
            content=new_user_message,
        )

        messages = self._build_messages(conversation)

        start_time = time.time()
        full_response = ""
        input_tokens = 0
        output_tokens = 0

        with self.client.messages.stream(
            model=settings.ANTHROPIC_MODEL,
            max_tokens=settings.ANTHROPIC_MAX_TOKENS,
            system=self._get_system_prompt(conversation, rag_context),
            messages=messages,
        ) as stream:
            for text in stream.text_stream:
                full_response += text
                yield text

            # Récupérer les métadonnées à la fin du stream
            final_message = stream.get_final_message()
            input_tokens = final_message.usage.input_tokens
            output_tokens = final_message.usage.output_tokens

        latency_ms = int((time.time() - start_time) * 1000)

        # Sauvegarder la réponse complète
        Message.objects.create(
            conversation=conversation,
            role="assistant",
            content=full_response,
            input_tokens=input_tokens,
            output_tokens=output_tokens,
            latency_ms=latency_ms,
        )

        # Mettre à jour les totaux de la conversation
        Conversation.objects.filter(id=conversation.id).update(
            total_input_tokens=F("total_input_tokens") + input_tokens,
            total_output_tokens=F("total_output_tokens") + output_tokens,
        )
```

### 2.4 View SSE — Streaming vers React

```python
# apps/ai/views.py
import json
from django.http import StreamingHttpResponse
from rest_framework.views import APIView
from rest_framework.permissions import IsAuthenticated

class ChatStreamView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request, conversation_id):
        conversation = get_object_or_404(
            Conversation, id=conversation_id, user=request.user
        )
        message = request.query_params.get("message", "").strip()
        if not message:
            return Response({"error": "Message vide."}, status=400)

        # Récupérer le contexte RAG si activé
        rag_context = ""
        if conversation.has_rag:
            rag_context = RAGService().retrieve_context(message, limit=3)

        service = ClaudeService()

        def event_stream():
            try:
                for token in service.stream_response(conversation, message, rag_context):
                    # Format SSE : "data: <json>\n\n"
                    yield f"data: {json.dumps({'token': token})}\n\n"

                # Signal de fin de stream
                yield f"data: {json.dumps({'done': True})}\n\n"

            except anthropic.RateLimitError:
                yield f"data: {json.dumps({'error': 'Limite d\'appels atteinte. Réessayez dans quelques secondes.', 'code': 'RATE_LIMIT'})}\n\n"
            except anthropic.APIError as e:
                yield f"data: {json.dumps({'error': 'Erreur du service IA.', 'code': 'AI_ERROR'})}\n\n"

        response = StreamingHttpResponse(event_stream(), content_type="text/event-stream")
        response["Cache-Control"] = "no-cache"
        response["X-Accel-Buffering"] = "no"   # Désactiver le buffering Nginx
        return response
```

---

## 3. RAG avec pgvector — Recherche sémantique

### 3.1 Configuration pgvector

```python
# requirements/base.txt
pgvector==0.x

# Migration pour activer l'extension
class Migration(migrations.Migration):
    operations = [
        migrations.RunSQL("CREATE EXTENSION IF NOT EXISTS vector"),
    ]

# apps/ai/models.py
from pgvector.django import VectorField

class Document(BaseModel):
    """Document indexé pour le RAG."""
    title = models.CharField(max_length=200)
    content = models.TextField()
    source = models.CharField(max_length=200, blank=True)   # URL ou référence
    embedding = VectorField(dimensions=1536, null=True)      # Dimensions = modèle d'embedding choisi

    class Meta:
        indexes = [
            # Index IVFFLAT pour la recherche approximative (plus rapide sur grandes collections)
            # Nécessite: CREATE INDEX CONCURRENTLY ON ai_document USING ivfflat (embedding vector_cosine_ops)
        ]
```

### 3.2 Service RAG

```python
# apps/ai/rag_service.py
import anthropic
from django.conf import settings
from .models import Document

class RAGService:
    def __init__(self):
        self.client = anthropic.Anthropic(api_key=settings.ANTHROPIC_API_KEY)

    def get_embedding(self, text: str) -> list[float]:
        """Obtenir l'embedding d'un texte via un modèle d'embedding."""
        # Option A : Utiliser l'API Voyage AI (recommandé avec Claude)
        # Option B : Utiliser OpenAI text-embedding-3-small
        # Option C : Sentence Transformers local (gratuit, moins performant)
        import voyageai
        vo = voyageai.Client()
        result = vo.embed([text], model="voyage-3", input_type="query")
        return result.embeddings[0]

    def index_document(self, title: str, content: str, source: str = "") -> Document:
        """Indexer un document — découper et embedder."""
        # Découper en chunks si le contenu est long
        chunks = self._split_into_chunks(content, chunk_size=500, overlap=50)

        documents = []
        for chunk in chunks:
            embedding = self.get_embedding(chunk)
            doc = Document.objects.create(
                title=title,
                content=chunk,
                source=source,
                embedding=embedding,
            )
            documents.append(doc)
        return documents

    def retrieve_context(self, query: str, limit: int = 3) -> str:
        """Récupérer les documents les plus similaires à la requête."""
        query_embedding = self.get_embedding(query)

        # Recherche par cosine similarity avec pgvector
        from pgvector.django import CosineDistance
        similar_docs = (
            Document.objects
            .alias(distance=CosineDistance("embedding", query_embedding))
            .filter(distance__lt=0.3)   # Seuil de similarité (0 = identique, 1 = opposé)
            .order_by(CosineDistance("embedding", query_embedding))
            [:limit]
        )

        if not similar_docs:
            return ""

        context_parts = [f"Source : {doc.source}\n{doc.content}" for doc in similar_docs]
        return "\n\n---\n\n".join(context_parts)

    def _split_into_chunks(self, text: str, chunk_size: int, overlap: int) -> list[str]:
        words = text.split()
        chunks = []
        i = 0
        while i < len(words):
            chunk = " ".join(words[i:i + chunk_size])
            chunks.append(chunk)
            i += chunk_size - overlap
        return chunks
```

---

## 4. Frontend React — Interface de chat streamée

### 4.1 Hook de streaming

```typescript
// features/ai/hooks/useChat.ts
import { useState, useRef, useCallback } from "react";

interface Message {
  id: string;
  role: "user" | "assistant";
  content: string;
  isStreaming?: boolean;
}

export function useChat(conversationId: string) {
  const [messages, setMessages] = useState<Message[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const abortControllerRef = useRef<AbortController | null>(null);

  const sendMessage = useCallback(async (userMessage: string) => {
    if (!userMessage.trim() || isLoading) return;

    // Ajouter le message utilisateur
    const userMsg: Message = {
      id: crypto.randomUUID(),
      role: "user",
      content: userMessage,
    };

    // Placeholder pour la réponse en cours de streaming
    const assistantMsgId = crypto.randomUUID();
    const assistantMsg: Message = {
      id: assistantMsgId,
      role: "assistant",
      content: "",
      isStreaming: true,
    };

    setMessages(prev => [...prev, userMsg, assistantMsg]);
    setIsLoading(true);
    setError(null);

    abortControllerRef.current = new AbortController();

    try {
      const response = await fetch(
        `/api/v1/ai/stream/${conversationId}/?message=${encodeURIComponent(userMessage)}`,
        {
          headers: { Authorization: `Bearer ${getAccessToken()}` },
          signal: abortControllerRef.current.signal,
        }
      );

      const reader = response.body!.getReader();
      const decoder = new TextDecoder();

      while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        const chunk = decoder.decode(value);
        const lines = chunk.split("\n").filter(l => l.startsWith("data: "));

        for (const line of lines) {
          const data = JSON.parse(line.replace("data: ", ""));

          if (data.error) {
            setError(data.error);
            break;
          }

          if (data.done) {
            setMessages(prev => prev.map(m =>
              m.id === assistantMsgId ? { ...m, isStreaming: false } : m
            ));
            break;
          }

          if (data.token) {
            setMessages(prev => prev.map(m =>
              m.id === assistantMsgId
                ? { ...m, content: m.content + data.token }
                : m
            ));
          }
        }
      }
    } catch (err) {
      if ((err as Error).name !== "AbortError") {
        setError("Erreur de connexion. Réessayez.");
      }
    } finally {
      setIsLoading(false);
    }
  }, [conversationId, isLoading]);

  const stopGeneration = useCallback(() => {
    abortControllerRef.current?.abort();
    setIsLoading(false);
  }, []);

  return { messages, isLoading, error, sendMessage, stopGeneration };
}
```

### 4.2 Composant de chat

```tsx
// features/ai/components/ChatInterface.tsx
export function ChatInterface({ conversationId }: { conversationId: string }) {
  const { messages, isLoading, error, sendMessage, stopGeneration } = useChat(conversationId);
  const [input, setInput] = useState("");
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    sendMessage(input);
    setInput("");
  };

  return (
    <div role="log" aria-label="Conversation avec l'assistant" aria-live="polite">
      {messages.map((message) => (
        <div key={message.id} aria-label={`${message.role === "user" ? "Vous" : "Assistant"}`}>
          {/* Rendu Markdown pour la réponse IA */}
          {message.role === "assistant" ? (
            <MarkdownRenderer content={message.content} />
          ) : (
            <p>{message.content}</p>
          )}

          {/* Curseur clignotant pendant le streaming */}
          {message.isStreaming && (
            <span aria-hidden="true" className="animate-pulse">▋</span>
          )}
        </div>
      ))}

      <div ref={messagesEndRef} />

      {error && <p role="alert" className="text-error">{error}</p>}

      <form onSubmit={handleSubmit} aria-label="Envoyer un message">
        <textarea
          value={input}
          onChange={e => setInput(e.target.value)}
          onKeyDown={e => { if (e.key === "Enter" && !e.shiftKey) { e.preventDefault(); handleSubmit(e); } }}
          placeholder="Posez votre question..."
          aria-label="Votre message"
          disabled={isLoading}
          rows={3}
        />
        {isLoading ? (
          <button type="button" onClick={stopGeneration} aria-label="Arrêter la génération">
            ⏹ Arrêter
          </button>
        ) : (
          <button type="submit" disabled={!input.trim()} aria-label="Envoyer le message">
            Envoyer
          </button>
        )}
      </form>
    </div>
  );
}
```

---

## 5. Gestion des coûts et des limites

```python
# Middleware de vérification des quotas
class AICostGuard:
    DAILY_TOKEN_LIMIT = 100_000     # Par utilisateur
    MONTHLY_COST_LIMIT_USD = 10.0   # Par utilisateur

    @classmethod
    def check_quota(cls, user):
        from django.utils import timezone
        today = timezone.now().date()
        month_start = today.replace(day=1)

        daily_tokens = Message.objects.filter(
            conversation__user=user,
            role="assistant",
            created_at__date=today,
        ).aggregate(total=Sum("output_tokens"))["total"] or 0

        monthly_cost = Conversation.objects.filter(
            user=user,
            created_at__date__gte=month_start,
        ).aggregate(
            cost=Sum(
                F("total_input_tokens") * 3.0 / 1_000_000
                + F("total_output_tokens") * 15.0 / 1_000_000
            )
        )["cost"] or 0

        if daily_tokens >= cls.DAILY_TOKEN_LIMIT:
            raise PermissionDenied("Quota journalier atteint. Réessayez demain.")

        if monthly_cost >= cls.MONTHLY_COST_LIMIT_USD:
            raise PermissionDenied("Limite mensuelle atteinte.")
```

---

## 6. Checklist intégration IA

```
Backend :
  [ ] Clé API en variable d'environnement (jamais en dur)
  [ ] Gestion des erreurs Anthropic (RateLimitError, APIError, AuthenticationError)
  [ ] Timeout configuré sur les appels API (max 60s)
  [ ] Streaming SSE avec X-Accel-Buffering: no pour Nginx
  [ ] Sauvegarde de l'usage (input/output tokens) pour monitoring des coûts
  [ ] Quota par utilisateur implémenté

Frontend :
  [ ] Abort controller pour annuler le stream
  [ ] Curseur visuel pendant le streaming
  [ ] Gestion des erreurs (réseau, quota, API)
  [ ] Scroll automatique vers le bas lors du streaming
  [ ] Textarea avec Shift+Entrée pour saut de ligne, Entrée pour envoyer
  [ ] Accessible (aria-live="polite" sur le log de conversation)

RAG (si activé) :
  [ ] Extension pgvector installée
  [ ] Index ivfflat créé pour la performance
  [ ] Seuil de similarité calibré (0.3 = bon point de départ)
  [ ] Contexte injecté dans le system prompt, pas dans user message
```

---

## Références complémentaires

- `references/prompt-engineering.md` — Patterns de prompts pour différents cas d'usage (extraction, classification, génération structurée)
- `references/rag-evaluation.md` — Méthodes pour évaluer la qualité du RAG (précision, rappel, hallucination)
