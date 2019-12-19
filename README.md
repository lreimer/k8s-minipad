# K8s Minipad

Visualise and manage K8s deployments and pods using a Novation Launchpad Mini MK2.

## Building and Running

```bash
$ ./gradlew clean ass

$ ./gradlew run --args="-f src/test/resources/k8s-config.json"

$ kubectl apply -f src/test/resources/busybox-minipad-enabled.yaml
$ kubectl apply -f src/test/resources/nginx-minipad-enabled.yaml
$ kubectl apply -f src/test/resources/nginx-minipad-disabled.yaml
```

## Maintainer

M.-Leander Reimer (@lreimer), <mario-leander.reimer@qaware.de>

## License

This software is provided under the MIT open source license, read the `LICENSE`
file for details.
