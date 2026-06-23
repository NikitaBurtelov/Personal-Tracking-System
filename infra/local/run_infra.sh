echo 'Open Docker Desktop...'
open -a Docker

echo "Waiting for Docker to start..."
while ! docker info > /dev/null 2>&1; do
  sleep 1
  echo -n "."
done

echo ""
echo "Docker is running!"

echo 'Start infra [local]'
cd pts-docker
docker-compose up -d