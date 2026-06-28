mport sys
from google import genai

# Cambiá esto por tu API Key gratuita de aistudio.google.com
API_KEY = ""

print("📁 Leyendo los directorios de tu proyecto...")
contexto_codigo = ""

# Recorremos tu carpeta src buscando el código de tu bot
for root, dirs, files in os.walk("src"):
    for file in files:
        if file.endswith((".java", ".yaml", ".properties", ".sql")):
            full_path = os.path.join(root, file)
            try:
                # Forzamos la lectura en UTF-8 y manejamos errores de caracteres raros
                with open(full_path, "r", encoding="utf-8", errors="ignore") as f:
                    contenido = f.read()
                contexto_codigo += f"\n\n--- ARCHIVO: {file} ---\nRUTA: {full_path}\n"
                contexto_codigo += contenido
            except Exception as e:
                pass

if not contexto_codigo:
    print("❌ No se encontró código en la carpeta src.")
    sys.exit(1)

print("🚀 Enviando la estructura completa a Gemini...")

# Parche de seguridad para que los acentos en la ruta no rompan el string del prompt
prompt_texto = f"""
Actúa como un Ingeniero de Software Senior. Analizá en profundidad mi proyecto actual. 
Revisá la Arquitectura Hexagonal, los Handlers de conversación de WhatsApp y la persistencia en Postgres. 
Decime qué opinás del diseño y si encontrás algún peligro o bug.

Código del proyecto:
{contexto_codigo}
"""

# Limpiamos cualquier carácter rebelde antes de mandar a la API
prompt_seguro = prompt_texto.encode('utf-8', errors='ignore').decode('utf-8')

try:
    client = genai.Client(api_key=API_KEY)
    response = client.models.generate_content(
        model="gemini-2.5-flash",
        contents=prompt_seguro
    )
    print("\n🤖 **RESPUESTA DE GEMINI EN TU CONSOLA:**\n")
    print(response.text)
except Exception as e:
    print(f"❌ Error al conectar: {e}")
