#!/usr/bin/env bash

set -o errexit
set -o nounset
set -o pipefail

function main {
	local base="${1}"

	local path

	while IFS= read -r path
	do
		echo "$(_module "${path}" "${base}") ${path}"
	done
}

function _module {
	local base="${2}"
	local path="${1}"

	local dir=""
	local marker
	local markers="bnd.bnd build.xml gulpfile.js src/main/resources/application.properties"

	if [[ ${path} == workspaces/*-workspace/* ]]
	then
		markers="bnd.bnd client-extension.yaml"
	fi

	while [[ ${path} == */* ]]
	do
		dir="${dir:+${dir}/}${path%%/*}"
		path="${path#*/}"

		[[ ${dir} == modules ]] && continue

		for marker in ${markers}
		do
			if [[ -e ${dir}/${marker} ]] || { [[ ! -d ${dir} ]] && git cat-file -e "${base}:${dir}/${marker}" 2>/dev/null; }
			then
				echo "${dir}"

				return
			fi
		done
	done

	echo "-"
}

main "${@}"