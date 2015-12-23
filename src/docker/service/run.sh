find ${project.name}/plugins -name "*.jar" -type f -exec cp '{}' ${project.name}/plugins ';'
cd /
/${project.name}/bin/${project.name} $@