<script>
  import { onMount } from "svelte";

  const backendUrl = __BACKEND_URL__;
  let fullname = "";
  let email = "";
  let imgLink = "";
  let error = "";

  onMount(async () => {
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
      fullname = data.name;
      email = data.email;
      imgLink = data.imageLink;
      console.log(imgLink);
    } catch (e) {
      error = e.message;
    }
  });
</script>

<div class="profile">
  <h1>Profile tab</h1>
  {#if error}
    <p>Error: {error}</p>
  {:else}
    <div class="flex">
      <img src={imgLink} alt="Profile avatar" />
      <div class="flex-wrapper">
        <p>Username: {fullname}</p>
        <p>Email: {email}</p>
      </div>
    </div>
  {/if}
</div>

<style>
  .profile {
    border: 1px dashed #000;
    border-radius: 10%;
    padding: 15px;
  }

  .flex {
    display: flex;
    margin: 25px;
  }
  img {
    width: 50%;
    border-radius: 10%;
  }
  .flex-wrapper {
    border: 1px dashed #000;
    border-radius: 5%;
    margin-left: 25px;
    width: 100%;
    height: 85px;
    text-align: center;
  }
</style>
