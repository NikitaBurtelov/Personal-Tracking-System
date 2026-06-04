{{- define "pts-document-storage-service.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "pts-document-storage-service.fullname" -}}
{{- if .Values.fullnameOverride -}}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- $name := default .Chart.Name .Values.nameOverride -}}
{{- if contains $name .Release.Name -}}
{{- .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{- define "pts-document-storage-service.labels" -}}
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version | replace "+" "_" }}
app.kubernetes.io/name: {{ include "pts-document-storage-service.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}

{{- define "pts-document-storage-service.selectorLabels" -}}
app.kubernetes.io/name: {{ include "pts-document-storage-service.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end -}}

{{- define "pts-document-storage-service.serviceAccountName" -}}
{{- if .Values.serviceAccount.create -}}
{{- default (include "pts-document-storage-service.fullname" .) .Values.serviceAccount.name -}}
{{- else -}}
{{- default "default" .Values.serviceAccount.name -}}
{{- end -}}
{{- end -}}

{{- define "pts-document-storage-service.secretName" -}}
{{- if .Values.existingSecretEnv.name -}}
{{- .Values.existingSecretEnv.name -}}
{{- else if .Values.secretEnv.name -}}
{{- .Values.secretEnv.name -}}
{{- else -}}
{{- printf "%s-env" (include "pts-document-storage-service.fullname" .) -}}
{{- end -}}
{{- end -}}
