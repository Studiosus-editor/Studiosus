<script>
  import { onMount } from "svelte";

  let fullname = "";
  let email = "";
  let error = "";

  onMount(async () => {
    try {
      const response = await fetch("http://localhost:8080/api/profile", {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
        },
      });
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const data = await response.json();
      fullname = data.OAuthUserFullName;
      email = data.OAuthUserEmail;
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
    <p>Username: {fullname}</p>
    <p>Email: {email}</p>
  {/if}
</div>

<style>
  .profile {
    border: 1px dashed #000;
    border-radius: 10%;
    padding: 15px;
  }
</style>
