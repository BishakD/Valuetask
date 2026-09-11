/**
 * categorizeTodo — Firebase Cloud Function
 *
 * POST /categorizeTodo
 * Body:    { "text": "buy strawberries" }
 * Returns: { "category": "Shopping" }
 *
 * The Gemini API key is stored in Firebase Secret Manager and injected
 * at runtime via process.env.GEMINI_API_KEY — it is NEVER hardcoded.
 */

const { onRequest } = require("firebase-functions/v2/https");
const { defineSecret } = require("firebase-functions/params");
const { GoogleGenerativeAI, SchemaType } = require("@google/generative-ai");

// ---------------------------------------------------------------------------
// Secret declaration
// Firebase will inject this from Secret Manager at cold-start.
// Set it once with: firebase functions:secrets:set GEMINI_API_KEY
// ---------------------------------------------------------------------------
const geminiApiKey = defineSecret("GEMINI_API_KEY");

// ---------------------------------------------------------------------------
// System instruction (tested in AI Studio)
// ---------------------------------------------------------------------------
const SYSTEM_INSTRUCTION = `You are a to-do list categorizer. Given a short task or item, respond with exactly one category from this list: Market, Chores, Work, Personal, Health, Finance, Education, Habits, Family, Errands, Travel, Social. If nothing fits well, respond with 'Personal'. Always respond in JSON only, in this exact shape: {"category": "X"}`;

// ---------------------------------------------------------------------------
// Response schema — enforces { "category": string } at the SDK level
// ---------------------------------------------------------------------------
const RESPONSE_SCHEMA = {
  type: SchemaType.OBJECT,
  properties: {
    category: {
      type: SchemaType.STRING,
      description: "One of: Market, Chores, Work, Personal, Health, Finance, Education, Habits, Family, Errands, Travel, Social.",
      nullable: false,
    },
  },
  required: ["category"],
};

// ---------------------------------------------------------------------------
// Cloud Function
// ---------------------------------------------------------------------------
exports.categorizeTodo = onRequest(
  {
    // Bind the secret so it is available in process.env inside the function
    secrets: [geminiApiKey],
    // Allow unauthenticated callers (lock this down with Firebase App Check
    // or Auth if you add a front-end later)
    invoker: "public",
    // Keep the region explicit
    region: "us-central1",
  },
  async (req, res) => {
    // ── CORS pre-flight ───────────────────────────────────────────────────
    res.set("Access-Control-Allow-Origin", "*");
    if (req.method === "OPTIONS") {
      res.set("Access-Control-Allow-Methods", "POST");
      res.set("Access-Control-Allow-Headers", "Content-Type");
      res.set("Access-Control-Max-Age", "3600");
      return res.status(204).send("");
    }

    // ── Method guard ──────────────────────────────────────────────────────
    if (req.method !== "POST") {
      return res.status(405).json({ error: "Method not allowed. Use POST." });
    }

    // ── Input validation ──────────────────────────────────────────────────
    const { text } = req.body ?? {};
    if (!text || typeof text !== "string" || text.trim() === "") {
      return res
        .status(400)
        .json({ error: 'Missing or empty "text" field in request body.' });
    }

    // ── Gemini API call ───────────────────────────────────────────────────
    try {
      const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY);

      const model = genAI.getGenerativeModel({
        model: "gemini-3.6-flash",
        systemInstruction: SYSTEM_INSTRUCTION,
        generationConfig: {
          responseMimeType: "application/json",
          responseSchema: RESPONSE_SCHEMA,
          // Keep temperature low for deterministic classification
          temperature: 0.1,
        },
      });

      const result = await model.generateContent(text.trim());
      const raw = result.response.text();

      // Parse and validate the returned JSON
      let parsed;
      try {
        parsed = JSON.parse(raw);
      } catch {
        console.error("Gemini returned non-JSON:", raw);
        return res
          .status(500)
          .json({ error: "Model returned malformed JSON.", raw });
      }

      if (!parsed.category || typeof parsed.category !== "string") {
        console.error("Unexpected shape from Gemini:", parsed);
        return res
          .status(500)
          .json({ error: "Model response missing 'category' field.", raw });
      }

      return res.status(200).json({ category: parsed.category });
    } catch (err) {
      console.error("Gemini API error:", err);
      return res
        .status(500)
        .json({ error: "Failed to call Gemini API.", details: err.message });
    }
  }
);
