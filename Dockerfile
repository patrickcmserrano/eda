# Usamos a imagem oficial do Clojure com JDK 17
FROM clojure:temurin-17-tools-deps-focal

# Criamos e definimos o diretório de trabalho no container.
# O Polylith exige que a estrutura de diretórios (bases, components, projects) seja mantida.
WORKDIR /app

# Copiamos o workspace inteiro para o container. 
# Precisamos de tudo porque os jars locais (bases/components) são referenciados via :local/root.
COPY . .

# Definimos o diretório de trabalho para o projeto específico que queremos rodar.
# Isso permite que os caminhos relativos no deps.edn (ex: ../../bases/...) funcionem corretamente.
WORKDIR /app/projects/wallet

# Pré-carregamos as dependências para agilizar as próximas inicializações e garantir que o build está ok.
RUN clojure -P

# Expomos a porta da API (8080) e a porta do nREPL (7000)
EXPOSE 8080 7000

# O comando padrão para o container 'app' será rodar a API.
# Note: no docker-compose.yaml, o container 'worker' sobrescreve este comando.
CMD ["clojure", "-M", "-m", "br.com.eda.wallet-api.core"]
