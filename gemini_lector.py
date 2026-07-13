import os
import sys
from google import genai

# ✅ Configuración de tu clave copiada de AI Studio
API_KEY = "AQ.Ab8RN6I6j6RlJfqz7QXW25eojDGKuXAvR6PrEzl_5jiq1pr_dQ"  # <-- Pegá acá tu clave completa que termina en ...r_dQ

if API_KEY == "AIAQ.Ab8RN6I6j6RlJfqz7QXW25eojDGKuXAvR6PrEzl_5jiq1pr_dQ" or not API_KEY:
    print("❌ Por favor, poné tu API Key completa de Google AI Studio en la variable API_KEY.")
    sys.exit(1)

print("📁 Leyendo los directorios de Lunaris Ansenuza (Java, HTML, Properties)...")
contexto_codigo = ""

# Recorremos tu carpeta src buscando la lógica de negocio y vistas
for root, dirs, files in os.walk("src"):
    for file in files:
        if file.endswith((".java", ".yaml", ".properties", ".sql", ".html")):
            full_path = os.path.join(root, file)
            try:
                with open(full_path, "r", encoding="utf-8", errors="ignore") as f:
                    contenido = f.read()
                contexto_codigo += f"\n\n--- ARCHIVO: {file} ---\nRUTA: {full_path}\n"
                contexto_codigo += contenido
            except Exception:
                pass

if not contexto_codigo:
    print("❌ No se encontró código válido en la carpeta src.")
    sys.exit(1)

print("🚀 Enviando estructura completa a Gemini Cloud (Model: gemini-2.5-flash)...")

prompt_texto = f"""
Actúa como un Ingeniero de Software Senior. Analizá en profundidad mi proyecto actual. 
Revisá la Arquitectura Hexagonal, los Handlers de conversación de WhatsApp y la persistencia en Postgres. 
Decime qué opinás del diseño, si encontrás algún peligro, bug latente o deuda técnica en mis controladores o repositorios.

Código del proyecto:
{contexto_codigo}
"""

prompt_seguro = prompt_texto.encode('utf-8', errors='ignore').decode('utf-8')

try:
    client = genai.Client(api_key=API_KEY)
    response = client.models.generate_content(
        model="gemini-2.5-flash",
        contents=prompt_seguro
    )
    print("\n🤖 **ANÁLISIS CRÍTICO DE GEMINI EN TU CONSOLA:**\n")
    print(response.text)
except Exception as e:
    print(f"❌ Error al conectar con la API de Google: {e}")