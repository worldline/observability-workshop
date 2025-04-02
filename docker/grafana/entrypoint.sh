#!/usr/bin/env bash

# Check if the VSCODE_PROXY_URI environment variable is set
if [ -n "$VSCODE_PROXY_URI" ]; then
    # Replace the {{port}} template with 3000
    GF_SERVER_ROOT_URL="${VSCODE_PROXY_URI/\{\{port\}\}/3000}"
    
    # Export the new value as the GF_SERVER_ROOT_URL environment variable
    export GF_SERVER_ROOT_URL
fi

# Invoke the /run.sh script
/run.sh
