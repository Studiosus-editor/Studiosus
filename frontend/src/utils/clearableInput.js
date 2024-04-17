export function clearableInput(node) {
  const handleClear = event => {
    if (node.value !== "") {
      node.value = ""; 
      node.dispatchEvent(new Event('input'));
    }
  };

  node.addEventListener('click', handleClear);

  return {
    destroy() {
      node.removeEventListener('click', handleClear);
    }
  };
}
