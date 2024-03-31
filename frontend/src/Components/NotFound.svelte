<script>
  import { _ }  from 'svelte-i18n';
  import Error404 from "../assets/svg/error-404.svg";
</script>

<div>
  <img src={Error404} alt="" />
  <h1>{$_('notFound.error404')}</h1>
  <p>{$_('notFound.pageNotFound')}</p>
</div>

<style>
  div {
    margin: 200px 0;
    text-align: center;
  }
  img {
    width: 80%;
  }
</style>
