<script>
  import { onMount, onDestroy } from "svelte";
  import hljs from "highlight.js/lib/core";
  import yaml from "highlight.js/lib/languages/yaml";
  import "highlight.js/styles/atom-one-light.css";
  import { _ } from "svelte-i18n";
  import { textareaValueStore, overseerStore } from "./../scripts/store.js";
  import { addToast } from "../../Modal/ToastNotification/toastStore";

  let textarea;
  let overseerCode;
  let isLoggedIn = false;
  const backendUrl = __BACKEND_URL__;

  function isUserLoggedIn() {
    isLoggedIn = document.cookie.includes("JSESSIONID=");
  }

  const handleTextareaStyle = () => {
    textarea.style.height = "auto";
    textarea.style.height = textarea.scrollHeight + "px";
  };
  //Check if textarea has content
  function checkTextareaContent() {
    if ($textareaValueStore == null) {
      addToast({
        message: $_("assistantOverseer.noFileToFix"),
        type: "error",
      });
      return false;
    } else if ($textareaValueStore == "") {
      addToast({
        message: $_("assistantOverseer.noContentToFix"),
        type: "error",
      });
      return false;
    } else {
      return true;
    }
  }

  async function handleAssistant() {
    let message = $textareaValueStore;
    addToast({
      message: $_("assistantOverseer.fileIsBeingFixed"),
      type: "info",
    });
    await fetch(backendUrl + "/ai/fix", {
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
          addToast({
            message: $_("assistantOverseer.errorFixingFile"),
            type: "error",
          });
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.text();
      })
      .then((data) => {
        let formattedMessage = wrapWordsInSpan(data);
        overseerCode.innerHTML = formattedMessage;
        highlightSyntax();
      })
      .catch((error) => {
        console.error("Error:", error);
      });
  }

  //Parse response into span
  function wrapWordsInSpan(text) {
    return text
      .split(" ")
      .map((word) => `<span>${word}</span>`)
      .join(" ");
  }

  //Highlight the parsed response
  function highlightSyntax() {
    const codeElements = document.querySelectorAll("code.overseer-output");
    codeElements.forEach((element) => {
      let highlightedHtml = hljs.highlight(element.textContent, {
        language: "yaml",
      }).value;
      element.innerHTML = highlightedHtml;
    });
  }
  function handleStartOverseer() {
    if (checkTextareaContent() && isLoggedIn) {
      handleAssistant();
    }
    return;
  }
  onMount(() => {
    isUserLoggedIn(); //check if user is logged and assign to variable
    document.addEventListener("refresh-overseer", handleStartOverseer);
    if (overseerStore) {
      handleStartOverseer(); //start overseer if there is content in the textarea
    } else {
      handleStartOverseer();
    }
  });
  onDestroy(() => {
    document.removeEventListener("refresh-overseer", handleStartOverseer);
  });
</script>

<div id="fix-window">
  <div class="textarea-container">
    <code
      class="overseer-output"
      id="editor-field"
      cols="75"
      rows="30"
      spellcheck="false"
      bind:this={overseerCode}
      on:input={handleTextareaStyle}
      style="height: auto;"
    ></code>
  </div>
</div>

<style>
  #fix-window {
    display: flex;
    flex-direction: column;
    height: 100%;
    width: 50%;
    background: transparent;
  }

  #editor-field {
    padding-left: 10px;
    flex: 0 0 auto;
    width: 100%;
    height: 100%;
    line-height: 23px;
    overflow: none;
    font-size: 1rem;
    font-size: 18px;
    white-space: pre;
    background-color: rgb(217, 225, 235);
    border: none;
    outline: none;
    border: 1px solid var(--dodger-blue);
    resize: none;
    font-family: var(--ubuntu-mono);
    padding-right: 30px;
  }
  .textarea-container {
    overflow-y: auto;
    overflow-x: hidden;
    display: flex;
    flex: 1 1 auto;
    width: 100%;
    height: 100%;
    background: transparent;
    border: none;
    outline: none;
    resize: none;
    font-family: var(--ubuntu-mono);
    padding-right: 30px;
  }
  .overseer-output {
    overflow-y: auto;
  }
</style>
