<a name="v0.3.1"></a>
# v0.3.1 "Fatalist Zebra" (2020/09/14)


## Documentation

* **readme:** Update readme to reflect changes ([f94c86fe](https://github.com/ggranum/gradle-changelog-plugin/commits/f94c86fe))

## Features

* **build:** Release to Gradle Plugin Repository ([67d43100](https://github.com/ggranum/gradle-changelog-plugin/commits/67d43100))


## v0.3.0 "Broken Narwhal" (2020/09/14)


## Bug Fixes

* **windowsOS:** fix java.lang.string not found exception ([6bd6438b](https://github.com/ggranum/gradle-changelog-plugin/commits/6bd6438b))
* line return after each commit in sections without commit links printed ([1a9b1942](https://github.com/ggranum/gradle-changelog-plugin/commits/1a9b1942))
* issues ref can be in a oneliner commit message. Several issues can be closed in one message. Supports wider closing wording ([007bb752](https://github.com/ggranum/gradle-changelog-plugin/commits/007bb752))
* **GitHubService:** project obj should be passed to service ([cd0b3647](https://github.com/ggranum/gradle-changelog-plugin/commits/cd0b3647))

## Features

* **build:**
  * add self changelog plugin ([ab8505ca](https://github.com/ggranum/gradle-changelog-plugin/commits/ab8505ca))
  * Add support for Gradle 6+ ([a206f89a](https://github.com/ggranum/gradle-changelog-plugin/commits/a206f89a))
* added support of version name with param subtitle ([572d0dd7](https://github.com/ggranum/gradle-changelog-plugin/commits/572d0dd7))
* can append or overwrite existing changelog file. Default appends ([517bbc70](https://github.com/ggranum/gradle-changelog-plugin/commits/517bbc70))
* Improves previous tag detection by searching latest tagged commit across all branches. New logic for guessing next version using conventional commits tags present. ([9ce3e04d](https://github.com/ggranum/gradle-changelog-plugin/commits/9ce3e04d))
* **regexp:** add the refactor tag to the default filter regexp ([e9f2e37f](https://github.com/ggranum/gradle-changelog-plugin/commits/e9f2e37f))

## Refactor

* etracted string templates out methods
* move regexp for isolating fixes in a method
* **GitChangelogTask:** separate logic ina service and add unit tests
<a name="v0.2.0"></a>
## v0.2.0 (2019/04/18)


## Features

* Improves previous tag detection by searching latest tagged commit across all branches. New logic for guessing next version using conventional commits tags present. ([9ce3e04d](https://github.com/marcpoppleton/gradle-changelog-plugin/commits/9ce3e04d))
<a name="v0.1.3"></a>
# v0.1.3 "Electric Lama" (2015/04/14)


## Bug Fixes

* issues ref can be in a oneliner commit message. Several issues can be closed in one message. Supports wider closing wording ([007bb752](https://github.com/marcpoppleton/gradle-changelog-plugin/commits/007bb752))
* line return after each commit in sections without commit links printed ([1a9b1942](https://github.com/marcpoppleton/gradle-changelog-plugin/commits/1a9b1942))
* **GitHubService:** project obj should be passed to service ([cd0b3647](https://github.com/marcpoppleton/gradle-changelog-plugin/commits/cd0b3647))

## Features

* **regexp:** add the refactor tag to the default filter regexp ([e9f2e37f](https://github.com/marcpoppleton/gradle-changelog-plugin/commits/e9f2e37f))
* added suppor of version name with param subtitle ([572d0dd7](https://github.com/marcpoppleton/gradle-changelog-plugin/commits/572d0dd7))
* **build:** add self changelog plugin ([ab8505ca](https://github.com/marcpoppleton/gradle-changelog-plugin/commits/ab8505ca))

## Refactor

* move regexp for isolating fixes in a method
* etracted string templates out methods
* **GitChangelogTask:** separate logic ina service and add unit tests
