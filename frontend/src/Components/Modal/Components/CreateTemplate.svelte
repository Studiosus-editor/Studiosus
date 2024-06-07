<script>
  import { clearableInput } from "../../../utils/clearableInput.js";
  import { _ } from "svelte-i18n";
  import { createEventDispatcher, onMount } from "svelte";
  import { addToast } from "../../Modal/ToastNotification/toastStore.js";

  export let projectName = "";
  export let projectId = null;

  const backendUrl = __BACKEND_URL__;
  const MAX_PNAME_LENGTH = 22;
  const MAX_TEMPLATE_DESC = 600;
  const dispatch = createEventDispatcher();

  let description = "";

  function saveCurrentDescription() {
    localStorage.setItem("templateDesc-" + projectId, description);
  }

  async function handleCreateTemplate() {
    await fetch(backendUrl + `/api/project/${projectId}/template`, {
      method: "POST",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
      },
      credentials: "include",
      body: JSON.stringify({
        name: projectName,
        description: description,
      }),
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
      })
      .then(() => {
        addToast({
          message: $_("modalCreateTemplate.alertSuccess"),
          type: "success",
        });
        localStorage.removeItem("templateDesc-" + projectId);
        dispatch("closeTemplateModal");
      })
      .catch(() => {
        addToast({
          message: $_("modalCreateTemplate.alertError"),
          type: "error",
        });
        dispatch("closeTemplateModal");
      });
  }

  onMount(() => {
    const storedDescription = localStorage.getItem("templateDesc-" + projectId);
    if (storedDescription) {
      description = storedDescription;
    }
  });
</script>

<div class="content-wrapper">
  <div class="content-wrapper__name-wrapper">
    <h3>
      <label for="templateName">
        {$_("modalCreateTemplate.templateName")}</label
      >
    </h3>
    <input
      type="text"
      id="templateName"
      name="templateName"
      placeholder={projectName}
      bind:value={projectName}
      use:clearableInput
      maxlength={MAX_PNAME_LENGTH}
      on:focus={(event) => (event.target.placeholder = "")}
      on:blur={(event) => {
        if (event.target.value === "") {
          event.target.placeholder = $_(
            "modalCreateTemplate.placeholderMyTemplate"
          );
        }
      }}
    />
  </div>
  <div class="content-wrapper__description-container">
    <h3>
      <label for="templateDescription">
        {$_("modalCreateTemplate.description")}</label
      >
    </h3>
    <p>{$_("modalCreateTemplate.yourDescriptionIsAutomaticllySaved")}</p>
    <textarea
      id="templateDescription"
      name="templateDescription"
      on:blur={(event) => {
        if (event.target.value === "") {
          event.target.placeholder = $_(
            "modalCreateTemplate.provideAshortDescription"
          );
        }
      }}
      placeholder={$_("modalCreateTemplate.provideAshortDescription")}
      maxlength={MAX_TEMPLATE_DESC}
      on:change={saveCurrentDescription}
      bind:value={description}
    ></textarea>
  </div>
  <p class="information-message">
    <span>{$_("modalCreateTemplate.note")}</span>{$_(
      "modalCreateTemplate.noteText"
    )}
  </p>
  <div class="content-wrapper__button-container">
    <button
      class="button--blue"
      class:button-disabled={projectName.trim() === "" ||
        projectName.length > MAX_PNAME_LENGTH}
      on:click={handleCreateTemplate}
    >
      {$_("modalCreateTemplate.create")}
    </button>
  </div>
</div>

<style lang="scss">
  .content-wrapper {
    margin-top: 50px;
    display: flex;
    flex-direction: column;
    align-items: center;

    .information-message {
      margin-bottom: 0;
      span {
        color: var(--dark-cerulean);
      }
    }

    &__name-wrapper {
      display: flex;
      flex-direction: column;
      align-items: center;
      margin-bottom: 24px;
    }
    &__button-container {
      margin-top: 24px;
      width: 30%;
    }
    &__description-container {
      width: 100%;

      h3 {
        text-align: left;
        margin-bottom: 0;
      }
      p {
        margin-top: 0;
        text-align: left;
      }
      textarea {
        padding-left: 5px;
        width: 100%;
        height: 200px;
        font-family: "Rubik", sans-serif;
        font-weight: 400;
        font-size: 18px;
        box-shadow: 0px 4px 8px 0px rgba(0, 0, 0, 0.3);
        border-radius: 5px;
        border: 1px solid;
        resize: none;
        &::-webkit-scrollbar {
          width: 10px;
        }
        &::-webkit-scrollbar-track {
          background: var(--grey85);
        }

        &::-webkit-scrollbar-thumb {
          background-color: var(--periwinkle);
          border-radius: 5px;
          border: 1px solid var(--black);
          border-right: none;
        }
      }
    }
  }
  input {
    height: 40px;
    text-align: center;
    font-family: "Rubik", sans-serif;
    font-weight: 400;
    font-size: 24px;
    box-shadow: 0px 4px 8px 0px rgba(0, 0, 0, 0.3);
    border-radius: 5px;
    border: 1px solid;
  }

  h3 {
    margin-top: 0;
    font-weight: 500;
  }
  .button-disabled {
    background-color: var(--grey43);
    pointer-events: none;
  }
</style>
