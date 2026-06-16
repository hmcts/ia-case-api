#!/usr/bin/env bash
set -euo pipefail

PREFIX="${1:-}"
REPOSITORY="${2:-}"

if [[ -z "$PREFIX" || -z "$REPOSITORY" ]]; then
  echo "Usage: $0 <POD_PREFIX> <REPO_PATH>"
  exit 1
fi

echo "Tracking PREFIX=$PREFIX, REPOSITORY=$REPOSITORY"

# -----------------------------
# Get latest pod for prefix
# -----------------------------
get_latest_pod() {
  kubectl get pods -n ia --no-headers \
    | awk '{print $1}' \
    | grep "^${PREFIX}-" \
    | sort \
    | tail -n 1
}

POD_NAME=$(get_latest_pod)

if [[ -z "$POD_NAME" ]]; then
  echo "No pods found for prefix $PREFIX"
  exit 1
fi

echo "Initial pod: $POD_NAME"

# -----------------------------
# Main loop
# -----------------------------
while true; do

  POD_NAME=$(get_latest_pod)

  if [[ -z "$POD_NAME" ]]; then
    echo "No pod currently exists for prefix. Retrying..."
    sleep 3
    continue
  fi

  # Check readiness
  PHASE=$(kubectl get pod "$POD_NAME" -n ia -o jsonpath='{.status.phase}')
  READY=$(kubectl get pod "$POD_NAME" -n ia -o jsonpath='{.status.containerStatuses[0].ready}')

  if [[ "$PHASE" != "Running" || "$READY" != "true" ]]; then
    echo "Pod $POD_NAME not ready (phase=$PHASE, ready=$READY)"
    sleep 3
    continue
  fi

  NODE=$(kubectl get pod "$POD_NAME" -n ia -o jsonpath='{.spec.nodeName}')

  COUNT=$(kubectl get pods --all-namespaces \
  	--field-selector spec.nodeName="$NODE",status.phase!=Succeeded,status.phase!=Failed \
  	-o json | jq '.items | length')

  echo "$COUNT pods on node $NODE (active pod: $POD_NAME)"

  if (( COUNT > 28 )); then
    echo "Node overloaded -> deleting $POD_NAME"
    kubectl delete pod -n ia "$POD_NAME"

    echo "Waiting for replacement pod..."
    while true; do
      NEW_POD=$(get_latest_pod)

      if [[ -n "$NEW_POD" && "$NEW_POD" != "$POD_NAME" ]]; then
        POD_NAME="$NEW_POD"
        echo "Switched to new pod: $POD_NAME"
        break
      fi

      sleep 3
    done

    continue
  fi

  echo "Selected stable pod: $POD_NAME"
  break
done

# -----------------------------
# Update mirrord.json
# -----------------------------
MIRRORD_FILE="$REPOSITORY/.mirrord/mirrord.json"

if [[ ! -f "$MIRRORD_FILE" ]]; then
  echo "ERROR: $MIRRORD_FILE not found"
  exit 1
fi

tmp=$(mktemp)

jq --arg pod "$POD_NAME" \
  '.target.path = ("pod/" + $pod)' \
  "$MIRRORD_FILE" > "$tmp"

mv "$tmp" "$MIRRORD_FILE"

echo "successfully updated mirrord.json with pod/$POD_NAME"
echo -e "\a"
sleep 0.1
echo -e "\a"
sleep 0.1
echo -e "\a"
sleep 0.1
echo -e "\a"
sleep 0.1
echo -e "\a"
