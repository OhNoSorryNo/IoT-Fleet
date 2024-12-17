import docker
from docker.errors import NotFound, APIError

def pull_image(image_name: str) -> None:
    """
    Pull the specified Docker image from Docker Hub.
    """
    client = docker.from_env()
    print(f"Pulling image '{image_name}' ...")
    try:
        client.images.pull(image_name)
        print(f"Image '{image_name}' successfully pulled.")
    except APIError as e:
        print(f"Error pulling image '{image_name}': {e}")

def start_container(image_name: str, container_name: str, detach: bool = True, ports: dict = None) -> None:
    """
    Start a container from the specified image.
    """
    client = docker.from_env()
    print(f"Starting container '{container_name}' from image '{image_name}' ...")
    try:
        container = client.containers.run(image_name, name=container_name, detach=detach, ports=ports)
        print(f"Container '{container_name}' started. ID: {container.id}")
    except APIError as e:
        print(f"Error starting the container: {e}")

def stop_container(container_name: str) -> None:
    """
    Stop the specified container.
    """
    client = docker.from_env()
    print(f"Stopping container '{container_name}' ...")
    try:
        container = client.containers.get(container_name)
        container.stop()
        print(f"Container '{container_name}' has been stopped.")
    except NotFound:
        print(f"Container '{container_name}' not found.")
    except APIError as e:
        print(f"Error stopping the container: {e}")

if __name__ == "__main__":
    # Example calls:
    IMAGE = "nginx:latest"
    CONTAINER = "my_nginx_container"

    pull_image(IMAGE)
    start_container(IMAGE, CONTAINER, ports={"80/tcp": 8080})
    # ... later ...
    stop_container(CONTAINER)
