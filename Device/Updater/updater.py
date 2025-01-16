import docker
import uvicorn
from docker.errors import NotFound, APIError
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

app = FastAPI()

def pull_image(registry_url: str, image_name: str, tag: str = "latest") -> None:
    client = docker.from_env()
    full_image_name = f"{registry_url}/{image_name}:{tag}" if registry_url else f"{image_name}:{tag}"
    print(f"Pulling image '{full_image_name}' ...")
    try:
        client.images.pull(full_image_name)
        print(f"Image '{full_image_name}' successfully pulled.")
    except APIError as e:
        print(f"Error pulling image '{full_image_name}': {e}")
        raise e

def stop_container(container_name: str) -> None:
    client = docker.from_env()
    print(f"Stopping container '{container_name}' ...")
    try:
        container = client.containers.get(container_name)
        container.stop()
        print(f"Container '{container_name}' has been stopped.")
    except NotFound:
        print(f"Container '{container_name}' not found, nothing to stop.")
    except APIError as e:
        print(f"Error stopping the container: {e}")
        raise e

def remove_container(container_name: str) -> None:
    client = docker.from_env()
    print(f"Removing container '{container_name}' ...")
    try:
        container = client.containers.get(container_name)
        container.remove()
        print(f"Container '{container_name}' has been removed.")
    except NotFound:
        print(f"Container '{container_name}' not found, nothing to remove.")
    except APIError as e:
        print(f"Error removing the container: {e}")
        raise e

def start_container(image_name: str, container_name: str, detach: bool = True, ports: dict = None) -> None:
    client = docker.from_env()
    print(f"Starting container '{container_name}' from image '{image_name}' ...")
    try:
        container = client.containers.run(
            image_name,
            name=container_name,
            detach=detach,
            ports=ports
        )
        print(f"Container '{container_name}' started. ID: {container.id}")
    except APIError as e:
        print(f"Error starting the container: {e}")
        raise e

class ImageRequest(BaseModel):
    registry_url: str = None  # Optional Registry URL
    image_name: str
    tag: str = "latest"
    container_name: str = "Firmware"  # Default Firmware container name

@app.put("/update-image")
def update_image(request: ImageRequest):
    registry_url = request.registry_url
    image_name = request.image_name
    tag = request.tag
    container_name = request.container_name

    try:
        # Pull the new image
        pull_image(registry_url, image_name, tag)

        # Stop and remove the old container if it exists
        stop_container(container_name)
        remove_container(container_name)

        # Start a new container from the pulled image
        full_image_name = f"{registry_url}/{image_name}:{tag}" if registry_url else f"{image_name}:{tag}"
        start_container(full_image_name, container_name, ports={"80/tcp": 9191})

        return {"status": "updated", "image": full_image_name, "container": container_name}
    except APIError as e:
        raise HTTPException(status_code=400, detail=str(e))

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=9090)
