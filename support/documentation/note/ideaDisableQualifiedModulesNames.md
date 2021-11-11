# IDEA - disabling qualified module names
For disabling qualified module names in IDEA UI, apply the following procedure:

- open `.idea/gradle.xml`
- find the following position in XML document

      <project>
        <component name="GradleSettings">
          <option name="linkedExternalProjectsSettings">
            <GradleProjectSettings>

- inside of `<GradleProjectSettings>` element, add the following option

      <option name="useQualifiedModuleNames" value="false" />
