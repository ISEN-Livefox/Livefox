Horizontal Pods Autoscaling
===

Step 1: Metrics server
---

To begin, you have to install a metrics server in your kubernetes cluster to recover data like the consumption of the cpu or the memory.
To do that, you need to execute this command:
```
kubectl apply -f https://github.com/ISEN-Livefox/Livefox/blob/theo/metrics-server.yaml
```

Or to enter all these ones:
```
wget https://github.com/kubernetes-sigs/metrics-server/releases/download/v0.3.6/components.yaml
mv components.yaml metrics-server.yaml
```

Then, you have to edit the metrics-server.yaml file.

```
vi metrics-server.yaml
```

Go down to the deployment level, and replace it by:

```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: metrics-server
  namespace: kube-system
  labels:
    k8s-app: metrics-server
spec:
  selector:
    matchLabels:
      k8s-app: metrics-server
  template:
    metadata:
      name: metrics-server
      labels:
        k8s-app: metrics-server
    spec:
      serviceAccountName: metrics-server
      volumes:
      # mount in tmp so we can safely use from-scratch images and/or read-only containers
      - name: tmp-dir
        emptyDir: {}
      containers:
      - name: metrics-server
        image: k8s.gcr.io/metrics-server-amd64:v0.3.6
        imagePullPolicy: IfNotPresent
        args:
          - --cert-dir=/tmp
          - --secure-port=4443
          - --kubelet-preferred-address-types=InternalIP
          - --kubelet-insecure-tls
        ports:
        - name: main-port
          containerPort: 4443
          protocol: TCP
        securityContext:
          readOnlyRootFilesystem: true
          runAsNonRoot: true
          runAsUser: 1000
        volumeMounts:
        - name: tmp-dir
          mountPath: /tmp
      nodeSelector:
        kubernetes.io/os: linux
        kubernetes.io/arch: "amd64"
```
Deploy metrics-server.yaml:
```
kubectl apply -f metrics-server.yaml
```
Now, look if your metrics-server run:

```
kubectl get pods --all-namespaces
```

Wait few seconds and try to type this command to look if your metrics-server is up.
```
kubectl top nodes
```

Step 2: Limit your deploy
---

We have to limit the use of cpu and memory.

If you type the next command, you can see the description of the deployment.
```
kubectl describe deploy livefox-deploy
```
To edit livefox-deploy, you need to enter :
```
kubectl edit deploy livefox-deploy
```

Just after container write: write `terminationMessagePolicy: File`
```
resources:
  limits:
    memory: "500Mi"
  requests:
    memory: "50Mi"
    cpu: "100m"
```

Step 3: deploy hpa
---

For the last step, you have to deploy your horizontal pods autoscaler

```
wget https://github.com/ISEN-Livefox/Livefox/blob/theo/hpa-cpu.yaml
wget https://github.com/ISEN-Livefox/Livefox/blob/theo/hpa-memory.yaml
kubectl create -f hpa-cpu.yaml
kubectl create -f hpa-memory.yaml
```

Wait few seconds, and look if all run:
```
watch kubectl get hpa,pods
```
You have to see something like that:
```
NAME                                                 REFERENCE                   TARGETS   MINPODS   MAXPODS   REPLICAS   AGE
horizontalpodautoscaler.autoscaling/livefox-cpu      Deployment/livefox-deploy   1%/50%    1         5         1          3m22s
horizontalpodautoscaler.autoscaling/livefox-memory   Deployment/livefox-deploy   28%/50%   1         5         1          3m15s                                   
NAME                                  READY   STATUS    RESTARTS   AGE
pod/livefox-deploy-6678f47858-6przn   1/1     Running   0          4m14s    
```
If you want to test, you can connect it in your pods with this commands:
```
kubectl -it exec livefox-deploy-6678f47858-6przn -- sh
```
And install stress with: 
```
apt-get install stress
```
Example to test hpa-memory: 
```
stress --vm 1 --vm-bytes 100M
```
If you want to see all commands
```
stress --help
```
