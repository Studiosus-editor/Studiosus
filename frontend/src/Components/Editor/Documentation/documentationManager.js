// uses fetch to load locale files on demand
export async function loadDocumentation() {
  const cachedLocale = localStorage.getItem('cachedLocale') || 'en';
  const response = await fetch(`/docs/${cachedLocale}.json`);
  const data = await response.text();
  if (!data) {
    return;
  }
  return JSON.parse(data);
}

function createTooltip(item, span, tooltip) {
  if (item) {
    let rect = span.getBoundingClientRect();
    tooltip.style.left = `${rect.left - 50}px`;
    tooltip.style.top = `${rect.top + tooltip.offsetHeight - 40}px`;
    tooltip.style.display = 'block';
    tooltip.innerHTML = `<p>${item.description}</p><p>Example: <br><code class="example">${item.example}</code></p>`;
  }
}

export const handleMouseOver = async function(span, structure, value, tooltip) {
  loadDocumentation().then(data => {
    let item;
    if (!structure.parentKey) {
      item = data[null][value];
      if (item) {
        createTooltip(item, span, tooltip);
      }
    } else {
      item = data[structure.parentKey][value];
      if (item) {
        createTooltip(item, span, tooltip);
      }
    }
  }).catch(error => {
    console.error('Error:', error);
  });
};

export const handleMouseOut = function() {
  tooltip.style.display = 'none';
};

export function markAndCheckFound(obj, key, parentKey = null, grandParentKey = null) {
  // Helper function to recursively search the object
  function recursiveSearch(obj, parentKey, grandParentKey) {
    if (typeof obj !== 'object' || obj === null) return { found: false };

    for (let k in obj) {
      if (k === key) {
        // Check if it's an object and if it has 'found' property
        if (typeof obj[k] !== 'object' || obj[k] && !obj[k].found) {
          // Mark as found
          if (typeof obj[k] !== 'object') {
            obj[k] = { value: obj[k], found: true };
          } else {
            obj[k].found = true;
          }
          // If parentKey is a number, use grandParentKey
          return { found: true, parentKey: isNaN(parentKey) ? parentKey : grandParentKey };
        } else {
          // Skip if already marked as found
          return { found: false };
        }
      } else if (typeof obj[k] === 'object') {
        // Recursively search in nested objects
        const result = recursiveSearch(obj[k], k, parentKey);
        if (result.found) {
          return result;
        }
      }
    }
    return { found: false };
  }

  // Call the helper function and return the result
  return recursiveSearch(obj, parentKey, grandParentKey);
}