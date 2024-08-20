const TerserPlugin = require("terser-webpack-plugin");

config.optimization = config.optimization || {};
config.optimization.minimize = true;
config.optimization.minimizer = [
    new TerserPlugin({
        terserOptions: {
            mangle: true,    // Note: By default, mangle is set to true.
            compress: true, // Disable the transformations that reduce the code size.
            output: {
                beautify: false,
            },
        },
    }),
];