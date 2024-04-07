<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('secondaryEmail'); section>
    <#if section = "header">
        ${msg("secondaryEmailTitle")}
    <#elseif section = "form">
			<h2>${msg("secondaryEmailHello",(username!''))}</h2>
			<p>${msg("secondaryEmailText")}</p>
			<form id="kc-mobile-update-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
				<div class="${properties.kcFormGroupClass!}">
					<div class="${properties.kcLabelWrapperClass!}">
						<label for="secondaryEmail"class="${properties.kcLabelClass!}">${msg("secondaryEmailFieldLabel")}</label>
					</div>
					<div class="${properties.kcInputWrapperClass!}">
						<input type="email" id="secondaryEmail" name="secondaryEmail" class="${properties.kcInputClass!}"
									 value="${secondaryEmail}" required aria-invalid="<#if messagesPerField.existsError('secondaryEmail')>true</#if>"/>
              <#if messagesPerField.existsError('secondaryEmail')>
								<span id="input-error-mobile-number" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
										${kcSanitize(messagesPerField.get('secondaryEmail'))?no_esc}
								</span>
              </#if>
					</div>
				</div>
				<div class="${properties.kcFormGroupClass!}">
					<div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
						<input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" type="submit" value="${msg("doSubmit")}"/>
					</div>
				</div>
			</form>
    </#if>
</@layout.registrationLayout>
