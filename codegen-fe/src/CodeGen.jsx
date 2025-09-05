import { useState } from "react";

export default function CodeGen() {
  const [jsonInput, setJsonInput] = useState("");
  const [loading, setLoading] = useState(false);

  const handleGenerate = async () => {
    try {
      setLoading(true);

      const response = await fetch("/api/codegen/generate", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: jsonInput,
      });

      if (!response.ok) {
        throw new Error("Failed to generate project");
      }

      // Convert response to blob
      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);

      // Create download link
      const a = document.createElement("a");
      a.href = url;
      a.download = "generated-project.zip";
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);

    } catch (err) {
      console.error("Error:", err);
      alert("Error generating project");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: "2rem", fontFamily: "sans-serif" }}>
      <h1>Spring Boot Code Generator</h1>
      <textarea
        placeholder="Enter your JSON schema here..."
        value={jsonInput}
        onChange={(e) => setJsonInput(e.target.value)}
        rows={10}
        style={{ width: "100%", marginBottom: "1rem" }}
      />
      <br />
      <button
        onClick={handleGenerate}
        disabled={loading}
        style={{
          padding: "0.5rem 1rem",
          background: "#4CAF50",
          color: "white",
          border: "none",
          cursor: "pointer",
        }}
      >
        {loading ? "Generating..." : "Generate & Download"}
      </button>
    </div>
  );
}
