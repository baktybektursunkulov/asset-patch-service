## Getting started

#### Portainer docker web UI
Create portainer volume to persist data
```
docker volume create portainer_data
```
Run portainer
```
docker run --restart always --name portainer -d -p 9000:9000 -v /var/run/docker.sock:/var/run/docker.sock -v portainer_data:/data portainer/portainer
```
---
#### Create docker images
```
docker build -t kcell/asset-patch-service .
```

#### docker-compose commands
local-dev
```
docker-compose up -d
```
test-environment
```
docker-compose -f docker-compose-ps-test.yml up -d
```
prod-environment
```
docker-compose -f docker-compose-ps-release.yml up -d
```
