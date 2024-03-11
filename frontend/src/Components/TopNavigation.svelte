<script>
    import { onMount } from 'svelte';
    import ArrowDown from "../assets/svg/dropdown-arrow-icon-white.svg";
    import CloseIcon from "../assets/svg/exit-cross-white.svg";
    import HamburgerIcon from "../assets/svg/hamburger-menu-white.svg";

    let langDropdownVisible = false;
    let hamburgerDropdownVisible = false;
    let dropdownContainer;
    let dropdownTopMenu;
    let hamburgerButton;
    let langDropdownButtonText;
    // for now defined here, but will be implemented differntly once we have language support
    let dropdownItems = {
        option1: "LT",
        option2: "EN"};

    onMount(() => {
        dropdownContainer = document.querySelector('.dropdown');
        dropdownTopMenu = document.querySelector('.top-nav__list');
        hamburgerButton = document.querySelector('.hamburger--btn');
        langDropdownButtonText = document.querySelector('.dropdown--btn h3');
        document.addEventListener('click', handleDocumentClick);
        window.addEventListener('resize', handleWindowResize);
    });

    // closes the dropdown menu when the window is resized & changes icons (hamburger/exit)
    function handleWindowResize() {
        if (window.innerWidth > 768) {
            hamburgerDropdownVisible = false;
            if (dropdownTopMenu.classList.contains('show-menu')) {
                dropdownTopMenu.classList.remove('show-menu');
            }
            if (dropdownTopMenu.classList.contains('hide-menu')) {
                dropdownTopMenu.classList.remove('hide-menu');
            }
        }
    }

    // toggles the display of the language dropdown menu + arrow rotation on user click
    function handleLangDropdownClick() {
        langDropdownVisible = !langDropdownVisible;
    }

    // Closes the dropdown menus when user clicks outside of the dropdown container
    function handleDocumentClick(event) {
        if (!dropdownContainer.contains(event.target)) {
            langDropdownVisible = false;
        }
        // toggles the transition of the hamburger dropdown menu 
        if (!hamburgerButton.contains(event.target) && dropdownTopMenu.classList.contains("show-menu")){
            dropdownTopMenu.classList.remove('show-menu');
            dropdownTopMenu.classList.add('hide-menu');
            hamburgerDropdownVisible = !hamburgerDropdownVisible;
        }

    }
    // Toggles the display of the hamburger dropdown menu + changes the hamburger icon to an exit icon
    function handleHamburgerDropdownClick() {
        hamburgerDropdownVisible = !hamburgerDropdownVisible;
        if (dropdownTopMenu.classList.contains('show-menu')) {
            dropdownTopMenu.classList.remove('show-menu');
            dropdownTopMenu.classList.add('hide-menu');
        } else {
            dropdownTopMenu.classList.remove('hide-menu');
            dropdownTopMenu.classList.add('show-menu');
        }
    }
    // Changes the language displayed in the language dropdown button
    function handleLanguageSelect(event) {
        const selectedLanguage = event.target.textContent;
        langDropdownButtonText.textContent = selectedLanguage;
    }
    // Changes background color if page is active
    function isActivePage(page) {
        return window.location.pathname === page;
    }

</script>
<header class="top-nav">
    <div class="top-nav__logo-container">
        <a href="/">
            <div class="top-nav__logo">
                <h3>Studiosus</h3>
            </div>
        </a>
    </div>
    <div class="top-nav__button-container">
        <button type="button" class="hamburger--btn button--default" class:active={hamburgerDropdownVisible} on:click={handleHamburgerDropdownClick}>
            <img class="hamburger--btn__exit-icon" class:active={hamburgerDropdownVisible} src={CloseIcon} alt="close navigation">
            <img class="hamburger--btn__burger-icon {hamburgerDropdownVisible ? 'hidden' : 'active'}" src={HamburgerIcon} alt="open navigation">
        </button>
    </div>
    <menu class="top-nav__list">
        <a href="/editor"><li class="top-nav__item {isActivePage('/editor') ? 'active' : ''}"><h3>Editor</h3></li></a>
        <a href="/projects"><li class="top-nav__item {isActivePage('/projects') ? 'active' : ''}"><h3>Projects</h3></li></a>
        <a href="/templates"><li class="top-nav__item {isActivePage('/templates') ? 'active' : ''}"><h3>Templates</h3></li></a>
        <a href="/login"><li class="top-nav__item {isActivePage('/login') ? 'active' : ''}"><h3>Login</h3></li></a>
        <li class="dropdown" on:click={handleLangDropdownClick} on:keydown={handleLangDropdownClick}>
            <button type="button" class="dropdown--btn button--default" class:active={langDropdownVisible}>
                <h3>LT</h3>
                <img id="dropdown__arrow-icon" src={ArrowDown} class:active={langDropdownVisible} alt="toggle language select">
            </button>
            <div class="dropdown__dropdown-content" class:visible={langDropdownVisible}>
                {#each Object.values(dropdownItems) as item}
                    <div class="dropdown__item-container" on:click={handleLanguageSelect} on:keydown={handleLanguageSelect}>
                        <h3>{item}</h3>
                    </div>
                {/each}
            </div>
        </li>
    </menu>
</header>

<style lang="scss">

header {
    height: 86px;
    a {
        text-decoration: none;
        color: var(--white);
        width: 100%;
    }
}

.top-nav {
    color: var(--white);
    display: flex;
    width: 100%;

    &__logo-container {
        display: flex;
        background-color: var(--dark-cerulean);
        width: 50%;
    }

    &__logo {
        margin-left: 40px;

        &:hover {
            cursor: pointer;
        }

        h3 {
            font-weight: 400;
            font-family: "Kanit", sans-serif;
        }
    
    }

    &__list {
        background-color: var(--dark-cerulean);
        display: flex;
        list-style-type: none;
        align-items: center;
        justify-content: flex-end;
        padding: 0;
        margin: 0;
        width: 50%;


        &:global(.show-menu) {
            transform: translateY(27%);
            transition: transform 1s ease;
        }
        &:global(.hide-menu) {
            transform: translateY(-100%);
            transition: transform 1s ease;
        }
    }
    
    &__item {
        padding: 5px 15px;
        display: flex;
        justify-content: center;
        text-align: center;
        

        &:hover {
            background-color: var(--maastricht-blue);
            cursor: pointer;
        }
        &.active {
            background-color: var(--maastricht-blue);
        }
    }

    &__button-container {
        display: none;
    }
    
}


.hamburger--btn {
    border-radius: 10px;
    display: none;
    margin: 15px 15px 12px 15px;
    padding: 10px;

    &:hover {
        background-color: var(--maastricht-blue);
    }

    &__burger-icon{
        &.active {
            display: block;
        }

        &.hidden {
            display: none;
        }
    }

    &__exit-icon {
        display: none;
        &.active {
            display: block;
        }
    }

    img {
        width: 35px; 
    }

    &.active {
        background-color: var(--maastricht-blue);
        padding-bottom: 22px;
        margin-bottom: 0;
        border-bottom-right-radius: 0px;
        border-bottom-left-radius: 0px; 

        &:hover {
            background-color: var(--hippie-blue)
        }
    }
}

.dropdown {
    margin-right: 25px;
    &--btn {
        display: flex;
        align-items: center;
        padding: 5px 15px;
        width: 80px;

        &:hover {
            background-color: var(--maastricht-blue);
        }

        &.active {
            background-color: var(--maastricht-blue) !important;
        }

        img {
            width: 35px;
            transition: transform 0.3s ease;

            &.active {
                transform: rotate(90deg);
            }
        }
    }

    &__dropdown-content {
        display: none;
        position: absolute;
        background-color: var(--maastricht-blue);
        min-width: 75px;
        box-shadow: 0px 8px 16px 0px rgba(0,0,0,0.5);
        z-index: 1;

        &.visible {
            display: block;
        }
    }

    &__item-container {
        text-align: center;

        :hover {
            background-color: var(--hippie-blue);
            cursor: pointer;
        }

        h3 {
            margin: 0;
            padding: 24.5px;    
        }
    }
    
}

@media (max-width: 768px) {
    .top-nav {
        justify-content: space-between;
        flex-wrap: wrap;
        position: fixed;
        z-index: 5;

        &__logo-container {
            position: relative;
            z-index: 2;
        }

        &__button-container {
            width: 50%;
            display: flex;
            justify-content: flex-end;
            z-index: 2;
            background-color: var(--dark-cerulean);
        }

        &__list {
            transform: translateY(-100%);
            flex-direction: column;
            align-items: flex-start;
            background-color: var(--maastricht-blue);
            margin: 0;
            position: absolute;
            z-index: 1;
            width: 100%;
            opacity: 95%;
        }

        &__item {
            width: 100%;
            padding: 0;

            &:hover {
                background-color: var(--hippie-blue);
            }
            &.active {
                background-color: var(--hippie-blue);
            }
        }
    }

    .dropdown {
        display: none;
    }

    .hamburger--btn {
        display: block;
    }
}
</style>
