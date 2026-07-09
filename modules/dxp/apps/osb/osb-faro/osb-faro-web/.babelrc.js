const testEnvironment =
	process.env.BABEL_ENV === 'test' || process.env.NODE_ENV === 'test';

module.exports = {
	env: {
		test: {
			plugins: [
				['@babel/plugin-proposal-decorators', {legacy: true}],
				['@babel/plugin-proposal-class-properties', {loose: true}],
				'dynamic-import-node',
				'import-graphql',
				'transform-require-context'
			]
		}
	},
	plugins: ['graphql-tag', 'lodash', '@babel/plugin-syntax-dynamic-import'],
	presets: [
		'@babel/preset-react',
		[
			'@babel/preset-env',
			testEnvironment
				? {loose: true, targets: {node: 'current'}}
				: {
						corejs: '^2.5.7',
						loose: true,
						targets: {ie: '11'},
						useBuiltIns: 'usage'
					}
		]
	]
};
