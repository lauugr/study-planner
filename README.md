# Ejecución
Carpeta `study-planner`:
- Para compilar desde terminal `javac -cp "lib/*" -d bin src/agents/*.java` 
- Para ejecutar JADE: `java -cp "bin:lib/*" jade.Boot -gui user:agents.UserAgent data:agents.DataAgent recommender:agents.RecommenderAgent`