import { register, init, locale} from 'svelte-i18n';

// uses fetch to load locale files on demand
async function loadLocale(localeName) {
  const response = await fetch(`/lang/${localeName}.json`);
  if (!response.ok) {
    throw new Error(`Failed to load ${localeName} translation`);
  }
  return await response.json();
}

register('en', () => loadLocale('en'));
register('lt', () => loadLocale('lt'));

// initial locale is to english unless there is a cached locale
init({
  fallbackLocale: 'en',
  initialLocale: localStorage.getItem('cachedLocale') || 'en',
});

export const AVAILABLE_LOCALES = ['en', 'lt'];

export function changeLocale(chosenLocale) {
  const cachedLocale = localStorage.getItem('cachedLocale');
  if (cachedLocale !== chosenLocale) {
    localStorage.setItem('cachedLocale', chosenLocale);
    locale.set(chosenLocale);
  }
}