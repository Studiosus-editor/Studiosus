<script>
    export let flagIcon;
    export let languageName;
    export let onClick;

</script>

<div class="language-container" on:click={onClick} on:keydown={onClick}>
    <div class="language-container__icon-container">
        <img src={flagIcon} alt="...">
    </div>
    <div class="language-container__text-container">
        <p>{languageName}</p>
    </div>
</div>

<style lang="scss">
.language-container {
    display: flex;
    padding: 0 10px 0 10px;
    
    &__icon-container img {
        width: 40px;
        height: 40px;
    }
    &__text-container {
        display: flex;
        align-items: center;
        margin-left: 10px;

        p {
            margin: 0;
            font-family: "Rubik", sans-serif;
        }
    }

    &:hover {
        cursor: pointer;
        background-color: var(--white-smoke)
    }
}
</style>
