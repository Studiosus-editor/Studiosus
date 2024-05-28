<script>
  import { writable } from "svelte/store";

  // Load chat messages from local storage or initialize with an empty map
  const storedMessages = localStorage.getItem("chatMessages");
  export let chatMessages = writable(
    storedMessages ? new Map(JSON.parse(storedMessages)) : new Map()
  );
  export function isMapEmpty() {
    let storedMessages = localStorage.getItem("chatMessages");
    let messages = $chatMessages;
    return messages.size === 0 &&
      (!storedMessages || storedMessages === JSON.stringify([]))
      ? true
      : false;
  }
  // Function to add a message
  export function addMessage(id, sender, message, privileged) {
    chatMessages.update((messages) => {
      messages.set(id, { sender, message, privileged });
      localStorage.setItem(
        "chatMessages",
        JSON.stringify(Array.from(messages))
      );
      return messages;
    });
  }
  // Function to create a new map for chat messages
  export function createNewMap() {
    chatMessages.set(new Map());
    localStorage.removeItem("chatMessages");
  }
</script>

{#each Array.from($chatMessages) as [id, chatMessage] (id)}
  {#if chatMessage}
    <div class={chatMessage.privileged ? "messages" : "messages light"}>
      <div style="display: block;">
        <strong>
          {chatMessage.sender ? chatMessage.sender.name : "Unknown"}
        </strong>
      </div>
      <div>
        {#if chatMessage.privileged}
          {@html chatMessage.message}
        {:else}
          {chatMessage.message}
        {/if}
      </div>
    </div>
  {/if}
{/each}

<style>
  .messages {
    box-sizing: border-box;
    display: block;
    color: rgb(85, 85, 85);
    width: 100%;
    border-top: 1px solid var(--silver);
    border-bottom: 1px solid var(--silver);
    height: auto;
    /* width: 100%; */
    padding-left: 10px;
    padding-top: 10px;
    white-space: pre-wrap;
    word-wrap: break-word;
    word-break: normal;
  }
  :global(.messages code) {
    word-break: normal;
    word-wrap: break-word;
    max-width: 100px; /* limit width to 100px */
    white-space: pre-wrap; /* wrap long lines of text */
    overflow: auto; /* add a scrollbar if the content overflows */
  }
  .messages.light {
    background-color: #f0f0f0;
  }
</style>
