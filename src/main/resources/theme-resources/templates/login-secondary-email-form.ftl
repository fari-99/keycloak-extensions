<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
	<#if section = "header">
		${msg("secondaryEmailFormTitle",realm.displayName)}
	<#elseif section = "show-username">
		<h1>${msg("secondaryEmailFormCodeTitle", realm.displayName)}</h1>
	<#elseif section = "form">
		<form id="kc-form-login" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
			<div class="${properties.kcFormGroupClass!}">
				<label for="secondaryEmail"
						class="${properties.kcLabelClass!}">${msg("secondaryEmailFormLabel")}</label>

				<input tabindex="1" id="secondaryEmail"
						aria-invalid="<#if messagesPerField.existsError('secondaryEmail')>true</#if>"
						class="${properties.kcInputClass!}" name="secondaryEmail"
						value=""
						type="text" autofocus autocomplete="off"/>

				<#if messagesPerField.existsError('secondaryEmail')>
					<span id="input-error-username" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
						${kcSanitize(messagesPerField.get('secondaryEmail'))?no_esc}
					</span>
				</#if>

				<div class="${properties.kcFormGroupClass!} ${properties.kcFormSettingClass!}">
					<div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
						<input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" type="submit" value="${msg("doSubmit")}"/>
					</div>
				</div>
			</div>
		</form>
	<#elseif section = "info" >
		${msg("secondaryEmailFormInstruction")}
	</#if>
</@layout.registrationLayout>
