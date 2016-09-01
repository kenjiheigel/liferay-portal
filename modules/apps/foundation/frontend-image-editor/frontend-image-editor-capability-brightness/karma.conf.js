'use strict';

var babelPresetMetal = require('babel-preset-metal');
var karmaBabelPreprocessor = require('karma-babel-preprocessor');
var karmaChai = require('karma-chai');
var karmaFirefoxLauncher = require('karma-firefox-launcher');
var karmaCommonJs = require('karma-commonjs');
var karmaJunitReporter = require('karma-junit-reporter');
var karmaMocha = require('karma-mocha');
var karmaSinon = require('karma-sinon');

var babelOptions = {
	presets: [babelPresetMetal],
	sourceMap: 'both'
};

module.exports = function(config) {
	config.set({
		plugins: [
			karmaBabelPreprocessor,
			karmaChai,
			karmaFirefoxLauncher,
			karmaCommonJs,
			karmaJunitReporter,
			karmaMocha,
			karmaSinon
		],

		frameworks: ['mocha', 'chai', 'sinon', 'commonjs'],

		files: [
			'node_modules/metal-soy-bundle/build/bundle.js',
			'node_modules/html2incdom/src/*.js',
			'node_modules/metal*/src/**/*.js',
			'src/**/*.js',
			'test/**/*.js'
		],

		preprocessors: {
			'src/**/*.js': ['babel', 'commonjs'],
			'node_modules/html2incdom/src/*.js': ['babel', 'commonjs'],
			'node_modules/metal-soy-bundle/build/bundle.js': ['babel', 'commonjs'],
			'node_modules/metal*/src/**/*.js': ['babel', 'commonjs'],
			'test/**/*.js': ['babel', 'commonjs']
		},

		reporters: ['junit', 'progress'],

		browsers: ['Firefox'],

		babelPreprocessor: {options: babelOptions}
	});

	config.files.push(
		'node_modules/resemblejs/resemble.js',
		'node_modules/simulate-dom-event/index.js',
		{
			included: false,
			pattern: 'test/**/*.png'
		}
	);

	// DO NOT preprocess WebWorkers
	config.preprocessors['src/**/!(BrightnessWorker)*.js'] = config.preprocessors['src/**/*.js'];
	delete config.preprocessors['src/**/*.js'];
};