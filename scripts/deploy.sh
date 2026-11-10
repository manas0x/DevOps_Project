#!/usr/bin/env bash
set -euo pipefail
DEPLOY_DIR="${DEPLOY_DIR:-$HOME/devops-project3-jenkins}"
PORT="${PORT:-8081}"
JAR="target/deployment-demo-1.0.0.jar"
mkdir -p "$DEPLOY_DIR"
test -f "$JAR" || { echo "Build the JAR first: mvn package" >&2; exit 1; }
PIDFILE="$DEPLOY_DIR/app.pid"
if [[ -f "$PIDFILE" ]]; then
  old_pid=$(cat "$PIDFILE")
  if [[ "$old_pid" =~ ^[0-9]+$ ]] && kill -0 "$old_pid" 2>/dev/null; then
    if ps -p "$old_pid" -o args= | grep -Fq -- '-Dproject3.instance=jenkins-demo'; then
      kill "$old_pid"
      for unused in {1..20}; do kill -0 "$old_pid" 2>/dev/null || break; sleep 0.5; done
      if kill -0 "$old_pid" 2>/dev/null; then echo "Previous app did not stop" >&2; exit 1; fi
    else
      echo "PID belongs to another process; refusing to stop it" >&2; exit 1
    fi
  fi
fi
cp "$JAR" "$DEPLOY_DIR/deployment-demo.jar"
token="-$PPID-$RANDOM-$RANDOM"
PORT="$PORT" nohup java -Dproject3.instance=jenkins-demo "-Dproject3.token=$token" -jar "$DEPLOY_DIR/deployment-demo.jar" > "$DEPLOY_DIR/app.log" 2>&1 < /dev/null &
new_pid=$!
echo "$new_pid" > "$PIDFILE"
for unused in {1..20}; do
  if ! kill -0 "$new_pid" 2>/dev/null; then break; fi
  if [[ "$(curl -fsS --max-time 1 "http://127.0.0.1:$PORT/health" 2>/dev/null || true)" == "deployment-demo-ok$token" ]]; then
    echo "Deployment successful: http://localhost:$PORT (PID $new_pid)"; exit 0
  fi
  sleep 1
done
kill "$new_pid" 2>/dev/null || true
rm -f "$PIDFILE"
echo "Deployment failed; see $DEPLOY_DIR/app.log" >&2
exit 1
