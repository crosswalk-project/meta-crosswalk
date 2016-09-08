Yocto Layer for Crosswalk
============================

This layer adds the packages necessary for adding support for [Crosswalk Project](https://crosswalk-project.org/) to your Yocto distribution.

## Supported Yocto Configurations
* [Ostro™ OS XT](https://github.com/ostroproject/ostro-os-xt)

Usages
=====
### Ostro™ OS XT
#### Setup build environment
Please refer to [Building Ostro™ OS XT](https://github.com/ostroproject/ostro-os-xt#building-ostro-tm-os-xt) to setup build environment, assuming the `ostro-os-xt` is checked out at `/path/to/ostro-os-xt` folder.


#### Build Ostro™ OS XT image with Crosswalk layer
Checkout the `meta-crosswalk` to local folder, assuming `/path/to/meta-crosswalk`.

Add the `meta-crosswalk` layer to `/path/to/ostro-os-xt/build/conf/bblayers.conf`
```bitbake
OSTRO_XT_LAYERS += "/path/to/meta-crosswalk"
```

In `/path/to/ostro-os-xt/build/conf/local.conf`:

Add Crosswalk security flags configuration include file by adding
```bitbake
require /path/to/meta-crosswalk/include/ostro-xt-security-flags.inc
```

Add Crosswalk dependent recipes as supported recipes by adding
```bitbake
SUPPORTED_RECIPES_append = " /path/to/meta-crosswalk/include/ostro-xt-supported-recipes.txt"
```

Install Crosswalk into Ostro™ OS XT image by adding
```bitbake
OSTRO_XT_IMAGE_EXTRA_INSTALL_append = " crosswalk"
```

Build the Ostro™ OS XT image by executing
```
$ bitbake ostro-xt-image-noswupd
```

It would produce the image with Crosswalk binary.

#### Launch Crosswalk on Ostro™ OS XT image
Please refer to [Installation onto platform's internal storage](https://github.com/ostroproject/ostro-os-xt#installation-onto-platforms-internal-storage) of Ostro™ OS XT to install the image to supported IoT device, e.g. [Intel® Joule™ Module](https://software.intel.com/en-us/iot/hardware/joule).

After device boots to desktop, launch Crosswalk app by
```
$ xwalk /path/to/app/manifest.json
```

License
=======
Please see the LICENSE file for more information.
