<script>
  import { onMount } from "svelte";
  import { _ } from "svelte-i18n";
  import { addToast } from "../Modal/ToastNotification/toastStore.js";
  import SearchField from "./SearchField.svelte";
  import TemplateEntry from "./TemplateEntry.svelte";
  import Modal from "../Modal/Modal.svelte";
  import Pagination from "../UtilComponents/Pagination.svelte";
  import ViewOrReviewTemplate from "../Modal/Components/ViewOrReviewTemplate.svelte";
  import DeleteTemplate from "../Modal/Components/DeleteTemplate.svelte";
  import ResponseComment from "../Modal/Components/ResponseComment.svelte";

  let isLoggedIn = false;

  let globalTemplatesActive = true;
  let yourTemplatesActive = false;
  let reviewTemplatesActive = false;

  let showInformationModal = false;
  let showReviewModal = false;
  let showDeleteConfirmationModal = false;
  let showResponseCommentModal = false;

  let errorLoadingTemplates;
  let isAdmin = false;

  let yourTemplates = [];
  let globalTemplates = [];
  let reviewTemplates = [];

  let currentlyDisplayedGlobalTemplates = [];
  let currentlyDisplayedYourTemplates = [];
  let currentlyDisplayedReviewTemplates = [];

  const backendUrl = __BACKEND_URL__;
  const TEMPLATES_PER_PAGE = 12;

  let isLoading = true;
  let emptySearchResults = false;

  let templateId;
  let infoName;
  let infoOwnerName;
  let infoDescription;
  let infoPhase;
  let responseComment;

  let gridStyle = "";

  onMount(async () => {
    setUserLoggedIn();
    await initGetGlobalTemplates();
    if (isLoggedIn) {
      checkUserRole();
    }
  });

  function toggleInformationModal() {
    showInformationModal = !showInformationModal;
  }

  function toggleResponseCommentModal() {
    showResponseCommentModal = !showResponseCommentModal;
  }

  function initResponseCommentModal(event) {
    const { comment } = event.detail;
    responseComment = comment;
    toggleResponseCommentModal();
  }

  function setUserLoggedIn() {
    isLoggedIn = document.cookie.includes("JSESSIONID=");
  }

  function toggleActive(item) {
    if (item === "globalTemplates" && !globalTemplatesActive) {
      gridStyle = "";
      globalTemplatesActive = true;
      yourTemplatesActive = false;
      reviewTemplatesActive = false;

      initGetGlobalTemplates();
    } else if (
      item === "yourTemplates" &&
      !yourTemplatesActive &&
      !isLoading // prevents toggling when loading
    ) {
      if (!isLoggedIn) {
        addToast({
          message: $_("templates.pleaseLogInToViewYourTemplates"),
          type: "info",
        });
        return;
      }
      gridStyle = "";
      globalTemplatesActive = false;
      yourTemplatesActive = true;
      reviewTemplatesActive = false;

      initGetYourTemplates();
    } else if (
      item === "reviewTemplates" &&
      !reviewTemplatesActive &&
      !isLoading
    ) {
      gridStyle = "";
      globalTemplatesActive = false;
      yourTemplatesActive = false;
      reviewTemplatesActive = true;

      initReviewTemplates();
    }
  }

  async function initReviewTemplates() {
    reviewTemplates = [];
    isLoading = true;
    const fetchedReviewTemplates = await getReviewTemplates();
    if (fetchedReviewTemplates === null) {
      isLoading = false;
      errorLoadingTemplates = true;
      addToast({
        message: $_("templates.errorLoadingTemplatesToast"),
        type: "error",
      });
      return;
    }
    reviewTemplates = fetchedReviewTemplates;
    currentlyDisplayedReviewTemplates = reviewTemplates.slice(
      0,
      TEMPLATES_PER_PAGE
    );
    applyGridStyle(currentlyDisplayedReviewTemplates);
    isLoading = false;
  }

  async function getReviewTemplates() {
    return fetch(backendUrl + "/api/templates/pending", {
      method: "GET",
      headers: {
        Accept: "application/json",
      },
      credentials: "include",
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
      })
      .then((data) => {
        return data;
      })
      .catch(() => {
        return null;
      });
  }

  async function checkUserRole() {
    try {
      const response = await fetch(backendUrl + "/api/profile", {
        method: "GET",
        headers: {
          Application: "application/json",
        },
        credentials: "include",
      });
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const data = await response.json();
      if (data.isAdmin === "true") {
        isAdmin = true;
      }
    } catch (e) {
      error = e.message;
    }
  }

  async function initGetGlobalTemplates() {
    globalTemplates = [];
    isLoading = true;
    const fetchedGlobalTemplates = await getGlobalTemplates();
    if (fetchedGlobalTemplates === null) {
      isLoading = false;
      errorLoadingTemplates = true;
      addToast({
        message: $_("templates.errorLoadingTemplatesToast"),
        type: "error",
      });
      return;
    }
    globalTemplates = fetchedGlobalTemplates;
    currentlyDisplayedGlobalTemplates = globalTemplates.slice(
      0,
      TEMPLATES_PER_PAGE
    );
    applyGridStyle(currentlyDisplayedGlobalTemplates);
    isLoading = false;
  }

  function handleSearchResults(event) {
    emptySearchResults = false;
    globalTemplates = event.detail;
    currentlyDisplayedGlobalTemplates = globalTemplates.slice(
      0,
      TEMPLATES_PER_PAGE
    );
    applyGridStyle(currentlyDisplayedGlobalTemplates);
    if (currentlyDisplayedGlobalTemplates.length === 0) {
      emptySearchResults = true;
    }
  }

  function applyGridStyle(itemArray) {
    gridStyle = "";
    if (itemArray.length < 2) {
      gridStyle = "grid-template-columns: minmax(auto, 300px);";
    } else if (itemArray.length === 2) {
      gridStyle = "grid-template-columns: repeat(2, minmax(auto, 300px));";
    }
  }

  async function initGetYourTemplates() {
    yourTemplates = [];
    isLoading = true;
    const fetchedYourTemplates = await getYourTemplates();
    if (fetchedYourTemplates === null) {
      isLoading = false;
      errorLoadingTemplates = true;
      addToast({
        message: $_("templates.errorLoadingTemplatesToast"),
        type: "error",
      });
      return;
    }
    yourTemplates = fetchedYourTemplates;
    currentlyDisplayedYourTemplates = yourTemplates.slice(
      0,
      TEMPLATES_PER_PAGE
    );
    applyGridStyle(currentlyDisplayedYourTemplates);
    isLoading = false;
  }

  function isEmptyArray(arr) {
    return arr.length === 0;
  }

  async function getGlobalTemplates() {
    return fetch(backendUrl + "/api/templates", {
      method: "GET",
      headers: {
        Accept: "application/json",
      },
      credentials: "include",
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
      })
      .then((data) => {
        return data;
      })
      .catch(() => {
        return null;
      });
  }

  async function getYourTemplates() {
    return fetch(backendUrl + "/api/templates/my", {
      method: "GET",
      headers: {
        Accept: "application/json",
      },
      credentials: "include",
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
      })
      .then((data) => {
        return data;
      })
      .catch(() => {
        return null;
      });
  }

  function handleTemplateDelete() {
    toggleDeleteModal();
    initGetYourTemplates();
  }

  function handleInformationClick(event) {
    const { name, username, description } = event.detail;
    infoName = name;
    infoOwnerName = username;
    infoDescription = description;
    toggleInformationModal();
  }

  function toggleReviewModal() {
    showReviewModal = !showReviewModal;
  }

  function toggleDeleteModal() {
    showDeleteConfirmationModal = !showDeleteConfirmationModal;
  }

  function initDeleteModal(event) {
    const { id, name, phase } = event.detail;
    templateId = id;
    infoName = name;
    infoPhase = phase;
    toggleDeleteModal();
  }

  function handleReviewClick(event) {
    const { name, username, description, id } = event.detail;
    infoName = name;
    infoOwnerName = username;
    infoDescription = description;
    templateId = id;
    toggleReviewModal();
  }

  function handlePaginationChange(event) {
    if (globalTemplatesActive) {
      currentlyDisplayedGlobalTemplates = event.detail;
    } else if (yourTemplatesActive) {
      currentlyDisplayedYourTemplates = event.detail;
    } else if (reviewTemplatesActive) {
      currentlyDisplayedReviewTemplates = event.detail;
    }
  }

  async function addToYourProjects(event) {
    const { id, name } = event.detail;
    return fetch(backendUrl + `/api/template/${id}/project/${name}`, {
      method: "POST",
      headers: {
        Accept: "application/json",
      },
      credentials: "include",
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
      })
      .then(() => {
        addToast({
          message: $_("templates.templateAddedToProjects"),
          type: "success",
        });
      })
      .catch(() => {
        addToast({
          message: $_("templates.errorAddingTemplateToProjects"),
          type: "error",
        });
      });
  }
</script>

{#if showInformationModal}
  <Modal
    panelName={$_("templateViewOrReview.templateDescription")}
    width="500px"
    on:closeModal={toggleInformationModal}
    ><ViewOrReviewTemplate
      name={infoName}
      ownerName={infoOwnerName}
      description={infoDescription}
      on:closeInformationModal={toggleInformationModal}
    />
  </Modal>
{/if}
{#if showReviewModal}
  <Modal
    panelName={$_("templateViewOrReview.templateReview")}
    width="500px"
    on:closeModal={toggleReviewModal}
    ><ViewOrReviewTemplate
      name={infoName}
      ownerName={infoOwnerName}
      description={infoDescription}
      {isAdmin}
      {templateId}
      on:closeReviewModal={toggleReviewModal}
      on:refreshReviewTemplates={initReviewTemplates}
    />
  </Modal>
{/if}
{#if showDeleteConfirmationModal}
  <Modal
    panelName={$_("deleteTemplateModal.modalTitle")}
    width="500px"
    on:closeModal={toggleDeleteModal}
    ><DeleteTemplate
      {templateId}
      templateName={infoName}
      phase={infoPhase}
      on:handleTemplateDeleted={handleTemplateDelete}
      on:closeDeleteModal={toggleDeleteModal}
    /></Modal
  >
{/if}
{#if showResponseCommentModal}
  <Modal
    panelName={$_("responseCommentModal.modalTitle")}
    width="500px"
    on:closeModal={toggleResponseCommentModal}
    ><ResponseComment
      {responseComment}
      on:closeResponseCommentModal={toggleResponseCommentModal}
    /></Modal
  >
{/if}
<div class="templates">
  <div class="templates__top-section">
    <h1>{$_("templates.templates")}</h1>
    <div class="templates__line"></div>
    <div class="templates__nav-wrapper">
      <div
        class:active={globalTemplatesActive}
        on:click={() => toggleActive("globalTemplates")}
        on:keydown={() => toggleActive("globalTemplates")}
      >
        <h3>{$_("templates.publicTemplates")}</h3>
      </div>
      <div
        class:disabled={!isLoggedIn}
        class:active={yourTemplatesActive}
        on:click={() => toggleActive("yourTemplates")}
        on:keydown={() => toggleActive("yourTemplates")}
      >
        <h3>{$_("templates.yourTemplates")}</h3>
      </div>
      {#if isAdmin}
        <div
          class:active={reviewTemplatesActive}
          on:click={() => toggleActive("reviewTemplates")}
          on:keydown={() => toggleActive("reviewTemplates")}
        >
          <h3>{$_("templates.pendingTemplates")}</h3>
        </div>
      {/if}
    </div>
    {#if isLoading}
      <div>{$_("templates.loadingTemplates")}</div>
    {:else if errorLoadingTemplates}
      <div>{$_("templates.errorLoadingTemplates")}</div>
    {:else if globalTemplatesActive}
      <div class="templates__search-container">
        <SearchField on:searchResults={handleSearchResults} />
      </div>
      <Pagination
        itemsPerPage={TEMPLATES_PER_PAGE}
        itemArray={globalTemplates}
        on:pageChange={handlePaginationChange}
      />
    {:else if yourTemplatesActive}
      <Pagination
        itemsPerPage={TEMPLATES_PER_PAGE}
        itemArray={yourTemplates}
        on:pageChange={handlePaginationChange}
      />
    {:else if reviewTemplatesActive}
      <Pagination
        itemsPerPage={TEMPLATES_PER_PAGE}
        itemArray={reviewTemplates}
        on:pageChange={handlePaginationChange}
      />
    {/if}
  </div>
  <div class="templates__bottom-section" style={gridStyle}>
    {#if globalTemplatesActive && !isLoading && !errorLoadingTemplates}
      {#if emptySearchResults}
        <h3>{$_("templates.emptySearchResults")}</h3>
      {:else}
        {#each currentlyDisplayedGlobalTemplates as template}
          <TemplateEntry
            id={template.id}
            name={template.name}
            description={template.description}
            username={template.ownerName}
            isGlobalTemplate={true}
            {isLoggedIn}
            on:informationClick={handleInformationClick}
            on:copyToProject={addToYourProjects}
          />
        {/each}
      {/if}
    {:else if yourTemplatesActive && !isLoading && !errorLoadingTemplates}
      {#if isEmptyArray(yourTemplates)}
        <h3>{$_("templates.noYourTemplatesFirstLine")}</h3>
        <h3>{$_("templates.noYourTemplatesSecondLine")}</h3>
      {:else}
        {#each currentlyDisplayedYourTemplates as template}
          <TemplateEntry
            id={template.id}
            name={template.name}
            description={template.description}
            phase={template.phase}
            reviewComment={template.comment}
            on:deleteTemplate={initDeleteModal}
            on:informationClick={handleInformationClick}
            on:viewResponseComment={initResponseCommentModal}
          />
        {/each}
      {/if}
    {:else if reviewTemplatesActive && !isLoading && !errorLoadingTemplates}
      {#if isEmptyArray(reviewTemplates)}
        <h3>{$_("templates.noPendingTemplates")}</h3>
      {:else}
        {#each currentlyDisplayedReviewTemplates as template}
          <TemplateEntry
            id={template.id}
            name={template.name}
            description={template.description}
            username={template.ownerName}
            {isAdmin}
            on:informationClick={handleInformationClick}
            on:reviewClick={handleReviewClick}
            on:copyToProject={addToYourProjects}
          />
        {/each}
      {/if}
    {/if}
  </div>
</div>

<style lang="scss">
  .templates {
    margin-top: 100px;
    margin-bottom: 100px;
    width: 100%;
    height: 1300px;

    &__bottom-section {
      display: grid;
      grid-template-columns: repeat(3, minmax(auto, 300px));
      gap: 10px;
      justify-content: center;
      text-align: center;
      width: 100%;
    }

    &__top-section {
      display: flex;
      flex-direction: column;
      align-items: center;
      margin-bottom: 25px;
    }

    &__line {
      min-width: 200px;
      max-width: 500px;
      width: 100%;
      height: 2px;
      background-color: black;
    }

    &__search-container {
      margin-bottom: 40px;
      display: flex;
      justify-content: center;
      align-items: center;
      width: 100%;
    }

    & h1 {
      margin: 0px;
    }

    &__nav-wrapper {
      display: flex;
      text-align: center;
      margin-bottom: 40px;

      & div {
        padding: 10px 20px;

        &:hover {
          cursor: pointer;
          font-weight: bold;
        }
      }
      div.active {
        background-color: var(--whisper);
        font-weight: bold;
        cursor: default;
      }
      .disabled {
        color: grey;
        &:hover {
          font-weight: inherit;
        }
      }

      & h3 {
        margin: 0;
        font-weight: inherit;
      }
    }
  }

  @media (max-width: 980px) {
    .templates {
      height: 2050px;
      &__bottom-section {
        grid-template-columns: repeat(2, 40%);
      }
    }
  }
  @media (max-width: 650px) {
    .templates {
      height: 2950px;
      &__bottom-section {
        grid-template-columns: repeat(1, minmax(300px, 80%));
      }
    }
  }
</style>
