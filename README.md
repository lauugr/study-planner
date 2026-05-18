# Ejecución
Carpeta `study-planner`:
- Para compilar desde terminal `javac -cp "lib/*" -d bin src/agents/*.java src/utils/*.java` 
- Para ejecutar JADE: `java -cp "bin:lib/*" jade.Boot -gui "data:agents.DataAgent;recommender:agents.RecommenderAgent;user:agents.UserAgent"`