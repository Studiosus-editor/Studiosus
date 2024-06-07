<script>
  import { nameStore, textareaValueStore } from "../scripts/store.js";
  import { onMount, afterUpdate } from "svelte";
  import sendIcon from "../../../assets/svg/Nav-controller/Send-icon.svg";
  import newChat from "../../../assets/svg/Nav-controller/new-chatw.svg";
  import ChatSystem from "./ChatSystem.svelte";
  import { _ } from "svelte-i18n";
  import * as marked from "marked";
  import DOMPurify from "dompurify";

  const backendUrl = __BACKEND_URL__;
  let inputValue = "";
  let message = "";
  let inputLock = false;
  let isLoading = false;
  let isLoggedIn = false;
  let outputContainer;
  let chatSystem;

  //check user authentication
  function isUserLoggedIn() {
    isLoggedIn = document.cookie.includes("JSESSIONID=");
  }

  //handle new chat creation
  function handleNewChatBtn() {
    if (!chatSystem.isMapEmpty()) {
      chatSystem.createNewMap();
      handleNewAssistant();
    }
  }
  async function handleNewAssistant() {
    await fetch(backendUrl + "/ai/newChat", {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        Accept: "application/json",
      },
      credentials: "include",
    }).then((response) => {
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return response.text();
    });
  }

  // main input handling function, primarily used for calling AI assistant
  async function handleInput() {
    chatSystem.addMessage(
      Date.now(),
      { name: $nameStore },
      DOMPurify.sanitize(inputValue),
      false
    );
    message =
      "This is the user code:" +
      $textareaValueStore +
      "\n" +
      "This is the user message:" +
      "\n" +
      inputValue;

    inputValue = "";
    const textarea = document.getElementById("Input-container");
    textarea.style.height = 39 + `px`;
    await handleAssistant(message);
  }

  // main function to handle the AI assistant prompt and response
  async function handleAssistant(message) {
    inputLock = true;
    isLoading = true;
    await fetch(backendUrl + "/ai/chat", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Accept: "application/json",
      },
      credentials: "include",
      body: message,
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.text();
      })
      .then((data) => {
        inputLock = false;
        isLoading = false;
        //Access chatsystem to add the response
        chatSystem.addMessage(
          Date.now(),
          { name: $_("editor.assistant.Assistant") },
          DOMPurify.sanitize(marked.parse(data)),
          true
        );
      })
      .catch((error) => {
        console.error("Error:", error);
        inputLock = false;
      });
  }
  $: {
    if ($nameStore === null || $nameStore.trim() === "") {
      nameStore.set("User");
    }
  }

  function hideAssistantComponents() {
    const outputContainer = document.getElementById("Output-container");
    const inputContainer = document.getElementById("Input-container");
    const inputController = document.getElementById("input-controller");
    const informContainer = document.getElementById("Inform-container");
    const newChatBtn = document.getElementById("new-chat-btn");

    outputContainer.style.display = "none";
    inputController.style.display = "none";
    inputContainer.style.display = "none";
    newChatBtn.style.display = "none";
    informContainer.style.display = "flex";
  }

  function adjustTextareaHeight() {
    const textarea = document.getElementById("Input-container");
    const singleLineHeight =
      textarea.clientHeight - textarea.offsetHeight + textarea.scrollHeight;
    const maxHeight = parseInt(getComputedStyle(textarea).maxHeight);

    textarea.style.height = `${singleLineHeight}px`;

    textarea.addEventListener("input", () => {
      textarea.style.height = "auto";
      if (
        textarea.scrollHeight > singleLineHeight &&
        textarea.scrollHeight < maxHeight
      ) {
        textarea.style.height = `${textarea.scrollHeight}px`;
      } else if (textarea.scrollHeight >= maxHeight) {
        textarea.style.height = `${maxHeight}px`;
      } else {
        textarea.style.height = `${singleLineHeight}px`;
      }
    });
  }

  $: isInputEmpty = !inputValue || inputValue.trim() === "";
  onMount(() => {
    isUserLoggedIn();
    if (!isLoggedIn) {
      hideAssistantComponents();
    } else {
      adjustTextareaHeight();
      const outputContainer = document.getElementById("Output-container");
      outputContainer.scrollTop = outputContainer.scrollHeight;
    }
  });

  afterUpdate(() => {
    outputContainer.scrollTop = outputContainer.scrollHeight;
  });
</script>

<div id="Assistant-container">
  <div id="Assistant-controller">
    <div id="textbox">{$_("editor.assistant.Chat")}</div>
    <button
      id="new-chat-btn"
      on:click={handleNewChatBtn}
      title={$_("editor.assistant.newChat")}
    >
      <img src={newChat} alt="New Chat" width="20px" height="20px" />
    </button>
  </div>
  <div id="Inform-container" style="display: none;">
    {$_("editor.assistant.logInToUseAssistant")}
    <a href="/login" style="text-decoration: none;"
      ><button id="login-button"> {$_("editor.assistant.signIn")}</button></a
    >
  </div>
  <div id="Output-container" bind:this={outputContainer}>
    <p class="Introduction-container">
      {$_("editor.assistant.greeting1")}
      {$nameStore}, {$_("editor.assistant.greeting2")}
    </p>
    <ChatSystem bind:this={chatSystem} />
  </div>
  <div id="input-controller">
    <textarea
      id="Input-container"
      bind:value={inputValue}
      placeholder={$_("editor.assistant.Question")}
    ></textarea>
    <button
      id="send-button"
      title={$_("editor.assistant.Send")}
      disabled={isInputEmpty || inputLock}
      class:disabled={isInputEmpty}
      on:click={handleInput}
    >
      {#if isLoading}
        <div class="loader"></div>
      {:else}
        <img src={sendIcon} alt="Send" />
      {/if}
    </button>
  </div>
</div>

<style>
  #Assistant-container {
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    padding-bottom: 10px;
    align-items: center;
    width: 100%;
    height: 100%;
    background-color: var(--white);
  }
  #Assistant-controller {
    color: var(--white);
    box-sizing: border-box;
    display: flex;
    flex-direction: row;
    align-items: left;
    justify-content: space-between;
    width: 100%;
    height: 40px;
    font-family: "Montserrat", sans-serif;
    background: linear-gradient(249.55deg, #6cb1ff 0%, #2f80ed 100%);
    border-bottom: 1px solid var(--silver);
  }
  #new-chat-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    margin-top: 10px;
    margin-bottom: 10px;
    border: none;
    background-color: transparent;
    width: 20px;
    height: 20px;
    margin: 10px;
  }
  #new-chat-btn:hover {
    cursor: pointer;
  }
  #textbox {
    display: flex;
    align-items: center;
    justify-content: center;
    font-family: "Montserrat", sans-serif;
    font-size: 14px;
    margin-left: 10px;
    background-color: transparent;
  }
  #Inform-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
    padding-top: 20px;
    justify-content: center;
    font-family: "Montserrat", sans-serif;
    font-size: 14px;
    width: 80%;
    height: 100px;
    margin-top: 20px;
    background-color: transparent;
  }
  #login-button {
    display: flex;
    margin-top: 20px;
    appearance: none;
    backface-visibility: hidden;
    background-color: var(--dodger-blue);
    border-radius: 10px;
    border-style: none;
    box-shadow: none;
    justify-content: center;
    align-items: center;
    box-sizing: border-box;
    color: var(--white);
    cursor: pointer;
    font-family: "Montserrat", sans-serif;
    font-size: 13px;
    font-weight: 500;
    letter-spacing: normal;
    line-height: 1.5;
    outline: none;
    overflow: hidden;
    padding: 14px 20px;
    position: relative;
    text-decoration: none;
    transform: translate3d(0, 0, 0);
    transition: all 0.3s;
    user-select: none;
    -webkit-user-select: none;
    touch-action: manipulation;
    vertical-align: top;
    white-space: nowrap;
    cursor: pointer;
  }
  #login-button:hover {
    background-color: var(--strong-blue);
    box-shadow:
      rgba(0, 0, 0, 0.05) 0 5px 30px,
      rgba(0, 0, 0, 0.05) 0 1px 4px;
    opacity: 1;
    transform: translateY(0);
    transition-duration: 0.35s;
  }

  #login-button:active {
    box-shadow:
      rgba(0, 0, 0, 0.1) 0 3px 6px 0,
      rgba(0, 0, 0, 0.1) 0 0 10px 0,
      rgba(0, 0, 0, 0.1) 0 1px 4px -1px;
    transform: translateY(5px);
    transition-duration: 0.35s;
  }
  .Introduction-container {
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    font-family: "Montserrat", sans-serif;
    font-size: 14px;
    padding-left: 7px;
    margin-bottom: 40px;
    width: 100%;
    height: 100px;
    background-color: transparent;
  }
  #Output-container {
    display: flex;
    flex-direction: column;
    font-family: "Montserrat", sans-serif;
    font-size: 14px;
    align-items: flex-start;
    overflow-y: auto;
    width: 100%;
    height: 88%;
    background-color: transparent;
  }
  #input-controller {
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: flex-start;
    background-color: var(--white);
    padding: 0;
    width: 90%;
    border-radius: 5px;
    margin-top: 10px;
    margin-bottom: 10px;
    background-color: transparent;
    border: 1px solid var(--silver);
  }
  #input-controller:focus-within {
    outline: 2px solid var(--dark-cerulean);
  }
  #Input-container {
    display: flex;
    align-items: center;
    justify-content: center;
    margin-top: 8px;
    margin-bottom: 8px;
    border-radius: 5px;
    font-size: 15px;
    width: 80%;
    overflow-y: auto;
    resize: none;
    border: none;
    font-family: "Montserrat", sans-serif;
    background-color: transparent;
    max-height: 300px;
  }
  #Input-container::-webkit-scrollbar {
    display: none;
  }
  #Input-container:focus {
    outline: none;
  }
  ::-webkit-scrollbar {
    width: 9px;
    height: 9px;
  }
  /* Track */
  ::-webkit-scrollbar-track {
    box-shadow: none;
    border-radius: 3px;
  }

  /* Handle */
  ::-webkit-scrollbar-thumb {
    background: var(--white);
    border: 1px solid var(--grey56);
    border-radius: 10px;
  }

  /* Handle on hover */
  ::-webkit-scrollbar-thumb:hover {
    background: transparent;
  }
  ::-webkit-scrollbar-corner {
    background: transparent;
  }
  #send-button {
    display: flex;
    align-items: center;
    justify-content: center;
    margin-top: 10px;
    margin-bottom: 10px;
    margin-left: 10px;
    border: none;
    background-color: transparent;
  }
  #send-button:hover {
    cursor: pointer;
  }
  #send-button:disabled {
    opacity: 0.5;
    cursor: default;
  }
</style>
