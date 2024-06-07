import { writable } from "svelte/store";

export const toasts = writable([]);

export const addToast = (toast) => {
  // Create a unique ID so we can easily find/remove it
  // if it is dismissible/has a timeout.
  const id = Math.floor(Math.random() * 10000);

  // Setup some sensible defaults for a toast.
  const defaults = {
    id,
    type: "info",
    dismissible: true,
    timeout: 3000,
  };

  // Merge the provided toast with the defaults
  const toastWithDefaults = { ...defaults, ...toast };

  // Push the toast to the top of the list of toasts
  toasts.update((all) => [toastWithDefaults, ...all]);

  // If toast is dismissible, dismiss it after "timeout" amount of time.
  if (toastWithDefaults.timeout) {
    setTimeout(() => dismissToast(id), toastWithDefaults.timeout);
  }
};

export const dismissToast = (id) => {
  toasts.update((all) => all.filter((t) => t.id !== id));
};