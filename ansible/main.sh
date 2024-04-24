#!/bin/bash

export ONE_AUTH=./ansible/ONE_AUTH
export ANSIBLE_HOST_KEY_CHECKING=False
export ANSIBLE_PRIVATE_KEY_FILE=./ansible/id_ed25519
CENDPOINT="https://grid5.mif.vu.lt/cloud3/RPC2"

# Succesfull VM creation prefix
prefix="VM ID:"

# # Enable connections Without VPN
NIC1="VNET2:NETWORK_UNAME=oneadmin:SECURITY_GROUPS=0"

# Create [WebServer VM]

# VM resources
CUSTOM_MEMORY="2048" # 2GB
CUSTOM_DISK_SIZE="20480" # 20GB

# # Create VM and capture the output into a variable
CVMREZ=$(onetemplate instantiate "debian12" --endpoint $CENDPOINT --memory $CUSTOM_MEMORY --name "WebServer VM" --disk "oneadmin[3745]:size="$CUSTOM_DISK_SIZE --nic $NIC1 --raw "\"TCP_PORT_FORWARDING=\"22 80 8080\"\"")

case $CVMREZ in
    "$prefix"*)
	WEBSERVER_VM_ID=$(echo $CVMREZ | cut -d ' ' -f 3)
	echo "[WebServer VM]: created successfully with ID: $WEBSERVER_VM_ID"
        ;;
    *)
    echo "[WebServer VM]: error creating VM: $CVMREZ"
    exit 1
        ;;
esac

# wait for VMs to be read
echo "Waiting for VMs to be ready..."
sleep 30

# GET details of [WebServer VM]

# Capture the output of the 'onevm show' command into a variable
vm_info=$(onevm show $WEBSERVER_VM_ID --endpoint $CENDPOINT)

# Get the public IP of the VM
WEBSERVER_VM_PUBLIC_IP=$(echo "$vm_info" | grep PUBLIC\_IP | cut -d '=' -f 2 | tr -d '"')

# # Get the private IP of the VM
WEBSERVER_VM_PRIVATE_IP=$(echo "$vm_info" | grep PRIVATE\_IP | cut -d '=' -f 2 | tr -d '"')

# # Get the TCP ports that are forwarded to the VM
WEBSERVER_VM_TCP_PORTS=$(echo "$vm_info" | grep TCP\_PORT\_FORWARDING | cut -d '=' -f 2 | tr -d '"')

# # Find to which port is port 80 forwarded
WEBSERVER_VM_TCP_PORT_80=$(echo "$WEBSERVER_VM_TCP_PORTS" | grep -oE '[0-9]+:80' | awk -F: '{print $1}')

# # Find to which port is port 22 forwarded
WEBSERVER_VM_TCP_PORT_22=$(echo "$WEBSERVER_VM_TCP_PORTS" | grep -oE '[0-9]+:22' | awk -F: '{print $1}')

echo "[WebServer VM]: Public IP: $WEBSERVER_VM_PUBLIC_IP:$WEBSERVER_VM_TCP_PORT_80"
echo "[WebServer VM]: Private IP: $WEBSERVER_VM_PRIVATE_IP"
# End of GET details of [WebServer VM]

ansible-playbook -i ./ansible/inventory.ini ./ansible/playbook.yml
if [ $? -ne 0 ]; then
    echo "[Webserver VM]: error running ansible playbook"
    exit 1
fi

echo "Website deployed successfully!"

exit 0