/**
 * categorizeTodo — Vercel Serverless Function
 *
 * POST /api/categorizeTodo
 * Body:    { "text": "buy strawberries" }
 * Returns: { "category": "Market" }
 *
 * The Gemini API key is stored as a Vercel Environment Variable
 * (Dashboard → Project → Settings → Environment Variables).
 * It is NEVER hardcoded in source.
 */

const { GoogleGenerativeAI, SchemaType } = require("@google/generative-ai");

// ---------------------------------------------------------------------------
// System instruction (your tested prompt from AI Studio)
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
      description:
        "One of: Market, Chores, Work, Personal, Health, Finance, Education, Habits, Family, Errands, Travel, Social.",
      nullable: false,
    },
  },
  required: ["category"],
};

// ---------------------------------------------------------------------------
// Vercel handler — plain Node.js (req, res) — no Firebase wrapper needed
// ---------------------------------------------------------------------------
module.exports = async function handler(req, res) {
  // ── CORS pre-flight ───────────────────────────────────────────────────────
  res.setHeader("Access-Control-Allow-Origin", "*");
  if (req.method === "OPTIONS") {
    res.setHeader("Access-Control-Allow-Methods", "POST");
    res.setHeader("Access-Control-Allow-Headers", "Content-Type");
    return res.status(204).end();
  }

  // ── Method guard ──────────────────────────────────────────────────────────
  if (req.method !== "POST") {
    return res.status(405).json({ error: "Method not allowed. Use POST." });
  }

  // ── Input validation ──────────────────────────────────────────────────────
  const { text } = req.body ?? {};
  if (!text || typeof text !== "string" || text.trim() === "") {
    return res
      .status(400)
      .json({ error: 'Missing or empty "text" field in request body.' });
  }

  // ── Gemini API call ───────────────────────────────────────────────────────
  try {
    const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY);

    const model = genAI.getGenerativeModel({
      model: "gemini-2.0-flash-latest",
      systemInstruction: SYSTEM_INSTRUCTION,
      generationConfig: {
        responseMimeType: "application/json",
        responseSchema: RESPONSE_SCHEMA,
        temperature: 0.1,
      },
    });

    const result = await model.generateContent(text.trim());
    const raw = result.response.text();

    let parsed;
    try {
      parsed = JSON.parse(raw);
    } catch {
      return res
        .status(500)
        .json({ error: "Model returned malformed JSON.", raw });
    }

    if (!parsed.category || typeof parsed.category !== "string") {
      return res
        .status(500)
        .json({ error: "Model response missing 'category' field.", raw });
    }

    return res.status(200).json({ category: parsed.category });
  } catch (err) {
    return res
      .status(500)
      .json({ error: "Failed to call Gemini API.", details: err.message });
  }
};
