# Pasos Previos
Para poder compilar y ejecutar y compilar este proyecto se necesita:
- **JDK version 8 o superior**
- **Libreria JADE** archivo `jade.jar`.
- **Libreria Commons Codec** archivo `commons-codec-1.3.jar`.

# Instalación
1. Clonar repositorio.
2. Asegurar que los archivos .jar se en cuentra en la carpeta `lib/`en el directorio raiz.
3. Verificar que el archivo de configuracion de reglas existe en la ruta `data/reglas_de_estudio.txt`.

# Ejecución
Carpeta `study-planner`:
- `rm -rf bin`
- Para compilar desde terminal `javac -cp "lib/*" -d bin src/agents/*.java src/utils/*.java` 
- Para ejecutar JADE: `java -cp "bin:lib/*" jade.Boot -gui "data:agents.DataAgent;recommender:agents.RecommenderAgent;user:agents.UserAgent"` (en caso de warning (Java 17): `java --add-opens java.base/java.lang=ALL-UNNAMED -cp "bin:lib/*" jade.Boot -gui "data:agents.DataAgent;recommender:agents.RecommenderAgent;user:agents.UserAgent"`)


# Casos de prueba
## Caso 1: plan de mantenimiento
**Entrada:**
```
¿Generar plan de estudios? (s/n): s
¿Cuántos días faltan para el examen?: 20
¿Cuántas horas al dia puedes estudiar?: 2
¿Cuál es tu nivel actual? (bajo/medio/alto): alto
¿Cuál es la dificultad de la asignatura? (baja/media/alta): baja
¿Qué porcentaje de temario llevas preparado? (0-100): 85
```
**Respuesta esperada:**
```
Plan recomendado: mantenimiento
Motivo: el nivel actual es alto y el porcentaje de temario preparado es elevado.
Distribución sugerida: 30% repaso, 50% simulacros, 20% puntos débiles
```

## Caso 2: plan intensivo
**Entrada:**
```
¿Generar plan de estudios? (s/n): s
¿Cuántos días faltan para el examen?: 5
¿Cuántas horas al dia puedes estudiar?: 2
¿Cuál es tu nivel actual? (bajo/medio/alto): medio
¿Cuál es la dificultad de la asignatura? (baja/media/alta): media
¿Qué porcentaje de temario llevas preparado? (0-100): 50
```
**Respuesta esperada:**
```
Plan recomendado: intensivo
Motivo: queda poco tiempo disponible o el porcentaje de temario pendiente es alto.
Distribución sugerida: 50% ejercicios, 30% teoría, 20% simulacros
```

## Caso 3: plan de refuerzo por nivel bajo
**Entrada:**
```
¿Generar plan de estudios? (s/n): s
¿Cuántos días faltan para el examen?: 15
¿Cuántas horas al dia puedes estudiar?: 2
¿Cuál es tu nivel actual? (bajo/medio/alto): bajo
¿Cuál es la dificultad de la asignatura? (baja/media/alta): media
¿Qué porcentaje de temario llevas preparado? (0-100): 60
```
**Respuesta esperada:**
```
Plan recomendado: refuerzo
Motivo: el nivel actual o la dificultad percibida de la asignatura requieren reforzar los contenidos.
Distribución sugerida: 40% teoría, 40% ejercicios guiados, 20% repaso
```

## Caso 4: plan de refuerzo por dificultad alta
**Entrada:**
```
¿Generar plan de estudios? (s/n): s
¿Cuántos días faltan para el examen?: 15
¿Cuántas horas al dia puedes estudiar?: 2
¿Cuál es tu nivel actual? (bajo/medio/alto): medio
¿Cuál es la dificultad de la asignatura? (baja/media/alta): alta
¿Qué porcentaje de temario llevas preparado? (0-100): 60
```
**Respuesta esperada:**
```
Plan recomendado: refuerzo
Motivo: el nivel actual o la dificultad percibida de la asignatura requieren reforzar los contenidos.
Distribución sugerida: 40% teoría, 40% ejercicios guiados, 20% repaso
```

## Caso 5: plan equilibrado
**Entrada:**
```
¿Generar plan de estudios? (s/n): s
¿Cuántos días faltan para el examen?: 15
¿Cuántas horas al dia puedes estudiar?: 3
¿Cuál es tu nivel actual? (bajo/medio/alto): medio
¿Cuál es la dificultad de la asignatura? (baja/media/alta): media
¿Qué porcentaje de temario llevas preparado? (0-100): 60
```
**Respuesta esperada:**
```
Plan recomendado: equilibrado
Motivo: hay margen suficiente para combinar teoría, práctica y repaso.
Distribución sugerida: 35% teoría, 45% práctica, 20% simulacros
```

## Caso 6: salida del sistema
**Entrada:**
```
¿Generar plan de estudios? (s/n): n
```
**Respuesta esperada:**
```
Finalizando servicio. Suerte con el estudio :)
```