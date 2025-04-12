#!/bin/sh

# Check if the VSCODE_PROXY_URI environment variable is set
if [ -n "$VSCODE_PROXY_URI" ]; then
    # Replace the {{port}} template with 3000
    PYROSCOPE_SERVER_ROOT_URL="${VSCODE_PROXY_URI/\{\{port\}\}/4040}"
    
    /usr/bin/pyroscope "$@" -api.base-url ${PYROSCOPE_SERVER_ROOT_URL}
else
    /usr/bin/pyroscope "$@"
fi
