/*
 *
 *  *
 *  * ******************************************************************************
 *  *
 *  *  Copyright (c) 2023-24 Harman International
 *  *
 *  *
 *  *
 *  *  Licensed under the Apache License, Version 2.0 (the "License");
 *  *
 *  *  you may not use this file except in compliance with the License.
 *  *
 *  *  You may obtain a copy of the License at
 *  *
 *  *
 *  *
 *  *  http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  **
 *  *  Unless required by applicable law or agreed to in writing, software
 *  *
 *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *
 *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *
 *  *  See the License for the specific language governing permissions and
 *  *
 *  *  limitations under the License.
 *  *
 *  *
 *  *
 *  *  SPDX-License-Identifier: Apache-2.0
 *  *
 *  *  *******************************************************************************
 *  *
 *
 */

package org.eclipse.ecsp.platform.notification.service;

import org.eclipse.ecsp.notification.dao.DynamicNotificationTemplateDAO;
import org.eclipse.ecsp.notification.dao.NotificationTemplateImportedDataDAO;
import org.eclipse.ecsp.notification.dao.RichContentDynamicNotificationTemplateDAO;
import org.eclipse.ecsp.notification.dao.RichContentImportedDataDAOMongoImpl;
import org.eclipse.ecsp.notification.entities.DynamicNotificationTemplate;
import org.eclipse.ecsp.platform.notification.exceptions.InvalidInputFileException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * DateFormatTransformerTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DateFormatTransformerTest {

    @InjectMocks
    NotificationTemplateService notificationTemplateService;

    @Mock
    private DynamicNotificationTemplateDAO dynamicNotificationTemplateDao;

    @Mock
    private NotificationTemplateImportedDataDAO notificationTemplateImportedDataDao;

    public static final String NOTIFICATION_ID_1 = "notificationId-1";

    private static String IMPORT_RICH_CONTENT_RESOURCES_PATH = "src/test/resources/importRichContent/";

    @InjectMocks
    private RichContentNotificationTemplateService richContentNotificationTemplateService;

    @Mock
    private RichContentDynamicNotificationTemplateDAO richContentDynamicNotificationTemplateDao;

    @Mock
    private RichContentImportedDataDAOMongoImpl richContentImportedDataDao;

    @Before
    public void setup() {
        initMocks(this);
    }

    @Test
    public void testVaildateHtmlrDateFormatContentTemplateSuccess() {
        String html =
            "<!doctype html>\n<html>\n<head>\n\t<meta charset=\"utf-8\">\n\t<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n\t<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">    \n    \n\t<title> PQRS Transactional Emails </title>\n\t<link href=\"https://fonts.googleapis.com/css?family=Roboto\" rel=\"stylesheet\">\n\t<style type=\"text/css\"> <!-- a, a:visited {text-decoration: none;} --> </style>\n\t<!--[if (gte mso 9)|(IE)]>\n\t\t<style type=\"text/css\"> .fallback-text {font-family: Arial, sans-serif !important;} </style>\n\t<![endif]-->\n</head>\n\n<body style=\"margin: 0; padding: 0; background: #ffffff;\">\n<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\" style=\"background: #ffffff;\">\n\t<tr>\n\t\t<td align=\"center\" style=\"margin: 0; padding: 0; vertical-align: top;\">\n\t\t\t\n\t\t\t<!--[if (gte mso 9)|(IE)]>\n\t\t\t<table align=\"center\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"600\">\n\t\t\t<tr>\n\t\t\t<td align=\"center\" valign=\"top\" width=\"600\">\n\t\t\t<![endif]-->\t\t\t\n\t\t\t\n\t\t\t<table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"max-width: 600px; width: 100%; background: #ffffff;\">\n\t\t\t\t<!-- ########################### HEADER ###########################  -->\n\t\t\t\t<tr>\n\t\t\t\t\t<td>\t\t\t\t\t\t\n\t\t\t\t\t\t<!-- Pre-Header -->\n\t\t\t\t\t\t<table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"max-width: 600px; width: 100%; line-height: normal;\">\n\t\t\t\t\t\t\t<tr>\n\t\t\t\t\t\t\t\t<td class=\"fallback-text\" style=\"margin: 0; padding: 5px; font-family: 'Roboto', Arial, Helvetica, sans-serif; font-size: 10px; line-height: 13px; text-align: left;\">\n\t\t\t\t\t\t\t\t\t<p style=\"color: #000000; margin: 0; padding: 0;\">\n\t\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t</p>\n\t\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t</tr>\n\t\t\t\t\t\t</table>\n\t\t\t\t\t\t\n\t\t\t\t\t\t<!-- Vehicle Brand Logo / Connectivity Logo -->\n\t\t\t\t\t\t<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"max-width: 600px; width: 100%; line-height: normal; text-align: center;\">\n\t\t\t\t\t\t\t<tr>\n\t\t\t\t\t\t\t\t<td style=\"margin: 0; padding: 7px; text-align: right; vertical-align: middle;\">\n\t\t\t\t\t\t\t\t\t<img src=\"https://myicnnct.deep.com/content/dam/cws/deep/email/logo_deep.png\" height=\"60\" style=\"margin: 0; padding: 0; border: none;\" alt=\"Image-hdr_brand\">\n\t\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t</tr>\n\t\t\t\t\t\t</table>\n\t\t\t\t\t\t\n\t\t\t\t\t\t<!-- Headline Text -->\n\t\t\t\t\t\t<table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"max-width: 600px; width: 100%; line-height: normal;\">\n\t\t\t\t\t\t\t<tr>\n\t\t\t\t\t\t\t\t<td class=\"fallback-text\" style=\"margin: 0; padding: 15px; background: #eeeeee; font-family: 'Roboto', Arial, Helvetica, sans-serif; font-size: 25px; line-height: 29px; text-align: center;\">\n\t\t\t\t\t\t\t\t\t<p style=\"color: #3F80C2; margin: 0; padding: 0; text-transform: uppercase;\">\n\t\t\t\t\t\t\t\t\t\tSERVIÇOS CONECTADOS\n\t\t\t\t\t\t\t\t\t</p>\n\t\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t</tr>\n\t\t\t\t\t\t</table>\n\t\t\t\t\t\t<!--  -->\n\t\t\t\t\t</td>\n\t\t\t\t</tr>\n\t\n\t\t\t\t<!-- ########################### BODY ###########################  -->\n\t\t\t\t<tr>\n\t\t\t\t\t<td>\n\t\t\t\t\t\t<table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"max-width: 600px; width: 100%; line-height: normal;\">\n\t\t\t\t\t\t\t<tr>\n\t\t\t\t\t\t\t\t<td class=\"fallback-text\" style=\"margin: 0; padding: 25px; color: #000000; font-family: 'Roboto', Arial, Helvetica, sans-serif; font-size: 16px; line-height: 23px; text-align: left;\">\n\t\t\t\t\t\t\t\t\t<!-- Name -->\n\t\t\t\t\t\t\t\t\t<p style=\"margin: 0; padding: 0; color: #000000;\">\n\t\t\t\t\t\t\t\t\t\t[$.Data.userProfile.firstName],\n\t\t\t\t\t\t\t\t\t</p>\n\t\t\t\t\t\t\t\t\t<img height=\"12\" style=\"margin: 0; padding: 0; border: none; display: block; max-width: 600px; width: 100%;\" src=\"https://myicnnct.deep.com/content/dam/cws/deep/email/spacer.gif\" alt=\"spacer\" />\n\t\t\t\t\t\t\t\t\t<!-- Body Text -->\n\t\t\t\t\t\t\t\t\t<p align=\"justify\" style=\"margin: 0; padding: 0; color: #000000;\">\n\t\t\t\t\t\t\t\t\t\tObrigado por assinar os Serviços Conectados para o seu [$.Data.vehicleProfile.name]. Seguem as informações da sua assinatura:\n\t\t\t\t\t\t\t\t\t<p align=\"justify\" style=\"margin: 0; padding: 0; color: #000000;\"> \n\t\t\t\t\t\t\t\t\tProduto: [$.Data.productName]\n\t\t\t\t\t\t\t\t\t<p align=\"justify\" style=\"margin: 0; padding: 0; color: #000000;\"> \n\t\t\t\t\t\t\t\t\tData da assinatura: [[formatDate|yyyy-MM-dd ss:mm:HH z|dd-MM-yyyy HH:mm:ss z|[$.Data.purchaseDate]]]\n\t\t\t\t\t\t\t\t\t<p align=\"justify\" style=\"margin: 0; padding: 0; color: #000000;\"> \n\t\t\t\t\t\t\t\t\tData de vencimento: [[formatDate|yyyy-MM-dd ss:mm:HH z|dd-MM-yyyy HH:mm:ss z|[$.Data.SubExpirationDate]]]\n\t\t\t\t\t\t\t\t\t<img height=\"12\" style=\"margin: 0; padding: 0; border: none; display: block; max-width: 600px; width: 100%;\" src=\"https://myicnnct.deep.com/content/dam/cws/deep/email/spacer.gif\" alt=\"spacer\" />\n\t\t\t\t\t\t\t\t\t<!-- Help Info -->\n\t\t\t\t\t\t\t\t\t<p align=\"justify\" style=\"margin: 0; padding: 0; color: #000000;\">\n\t\t\t\t\t\t\t\t\t\tPara informações adicionais, visite o website <a href=\"https://myicnnct.deep.com/br/pt/login\" >deep</a>  ou entre em contato com a Central de Serviços Conectados no 0800-007-7128.\n\t\t\t\t\t\t\t\t\t</p>\n\t\t\t\t\t\t\t\t\t<img height=\"25\" style=\"margin: 0; padding: 0; border: none; display: block; max-width: 600px; width: 100%;\" src=\"https://myicnnct.deep.com/content/dam/cws/deep/email/spacer.gif\" alt=\"spacer\" />\n\t\t\t\t\t\t\t\t\t<!--  -->\n\t\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t</tr>\n\t\t\t\t\t\t</table>\n\t\t\t\t\t\t<!--  -->\n\t\t\t\t\t</td>\n\t\t\t\t</tr>\n\t\n\t\t\t\t<!-- ########################### FOOTER ###########################  -->\n\t\t\t\t<tr>\n\t\t\t\t\t<td style=\"margin: 0; padding: 0;\">\n\t\t\t\t\t\t<!-- Brand Black/Grey -->\n\t\t\t\t\t\t \n\t\t\t\t\t\t<table width=\"600\" class=\"fallback-text\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background: #3e3e3e; color: #ffffff; max-width: 600px; width: 100%; font-family: 'Roboto', Arial, Helvetica, sans-serif; font-size: 10px; line-height: 14px; text-align: left; color: #ffffff;\">\n\t\t\t\t\t\t\t<tr>\n\t\t\t\t\t\t\t\t<td style=\"margin: 0; vertical-align: top;padding-left: 25px;padding-right: 25px;padding-top: 25px; vertical-align: top;\">\n\t\t\t\t\t\t\t\t\t<!-- Disclaimer -->\n\t\t\t\t\t\t\t\t\t<p align=\"justify\" style=\"margin: 0; padding: 0;\">\n\t\t\t\t\t\t\t\t\t\tOs Serviços Conectados estão disponíveis somente em veículos comprados no Brasil e equipados com o módulo de telemetria. Os Serviços Conectados somente poderão ser utilizados após serem ativados e em lugares com cobertura de rede de telefonia móvel celular. Os Serviços Conectados deverão ser ativados e cumprir os requisitos mínimos de assinatura. Ao utilizar os Serviços Conectados você aceita os Termos e Condições e a Política de Privacidade, que podem ser acessados através do link  <a href=\"https://myicnnct.deep.com/br/pt/login\" style=\"color: white;\" target=\"_blank\"><u>https://myicnnct.deep.com/br/pt/</u></a>.\n\t\t\t\t\t\t\t\t\t\t2021 Astellanti N.V.. Todos os direitos reservados. deep é uma marca registrada Astellanti N.V. Este é um e-mail automático, por favor, não responda.\n\t\t\t\t\t\t\t\t\t</p>\n\t\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t</tr>\n\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t<tr>\n\t\t\t\t\t\t\t\t<td style=\"margin: 0;vertical-align: top;padding-left: 25px;padding-right: 25px;padding-top: 25px; vertical-align: top;\">\n\t\t\t\t\t\t\t\t\t<!-- physical-address -->\n\t\t\t\t\t\t\t\t\t<p style=\"margin: 0; padding: 0;\">\n\t\t\t\t\t\t\t\t\t</p>\n\t\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t</tr>\n\t\t\t\t\t\t</table>\n\t\t\t\t\t</td>\n\t\t\t\t</tr>\n\t\n\t\t\t</table>\n\t\t\t\t\n\t\t\t<!--[if (gte mso 9)|(IE)]>\n\t\t\t</td>\n\t\t\t</tr>\n\t\t\t</table>\n\t\t\t<![endif]-->\n\t\t\t\t\n\t\t</td>\n\t</tr>\n</table>\n<!-- prevent Gmail on iOS font size manipulation -->\n<div style=\"display:none;white-space:nowrap;font:15px courier;line-height:0\">&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;</div>\n</body>\n</html>\n";
        assertEquals(0, notificationTemplateService.validateDateTransformerTemplate(html).size());
    }

    @Test
    public void testInvalidDateFormatTemplate() {
        String html =
            "<!doctype html>\n<html>\n<head>\n\t<meta charset=\"utf-8\">\n\t<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n\t<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">    \n    \n\t<title> PQRS Transactional Emails </title>\n\t<link href=\"https://fonts.googleapis.com/css?family=Roboto\" rel=\"stylesheet\">\n\t<style type=\"text/css\"> <!-- a, a:visited {text-decoration: none;} --> </style>\n\t<!--[if (gte mso 9)|(IE)]>\n\t\t<style type=\"text/css\"> .fallback-text {font-family: Arial, sans-serif !important;} </style>\n\t<![endif]-->\n</head>\n\n<body style=\"margin: 0; padding: 0; background: #ffffff;\">\n<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\" style=\"background: #ffffff;\">\n\t<tr>\n\t\t<td align=\"center\" style=\"margin: 0; padding: 0; vertical-align: top;\">\n\t\t\t\n\t\t\t<!--[if (gte mso 9)|(IE)]>\n\t\t\t<table align=\"center\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"600\">\n\t\t\t<tr>\n\t\t\t<td align=\"center\" valign=\"top\" width=\"600\">\n\t\t\t<![endif]-->\t\t\t\n\t\t\t\n\t\t\t<table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"max-width: 600px; width: 100%; background: #ffffff;\">\n\t\t\t\t<!-- ########################### HEADER ###########################  -->\n\t\t\t\t<tr>\n\t\t\t\t\t<td>\t\t\t\t\t\t\n\t\t\t\t\t\t<!-- Pre-Header -->\n\t\t\t\t\t\t<table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"max-width: 600px; width: 100%; line-height: normal;\">\n\t\t\t\t\t\t\t<tr>\n\t\t\t\t\t\t\t\t<td class=\"fallback-text\" style=\"margin: 0; padding: 5px; font-family: 'Roboto', Arial, Helvetica, sans-serif; font-size: 10px; line-height: 13px; text-align: left;\">\n\t\t\t\t\t\t\t\t\t<p style=\"color: #000000; margin: 0; padding: 0;\">\n\t\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t</p>\n\t\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t</tr>\n\t\t\t\t\t\t</table>\n\t\t\t\t\t\t\n\t\t\t\t\t\t<!-- Vehicle Brand Logo / Connectivity Logo -->\n\t\t\t\t\t\t<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"max-width: 600px; width: 100%; line-height: normal; text-align: center;\">\n\t\t\t\t\t\t\t<tr>\n\t\t\t\t\t\t\t\t<td style=\"margin: 0; padding: 7px; text-align: right; vertical-align: middle;\">\n\t\t\t\t\t\t\t\t\t<img src=\"https://myicnnct.deep.com/content/dam/cws/deep/email/logo_deep.png\" height=\"60\" style=\"margin: 0; padding: 0; border: none;\" alt=\"Image-hdr_brand\">\n\t\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t</tr>\n\t\t\t\t\t\t</table>\n\t\t\t\t\t\t\n\t\t\t\t\t\t<!-- Headline Text -->\n\t\t\t\t\t\t<table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"max-width: 600px; width: 100%; line-height: normal;\">\n\t\t\t\t\t\t\t<tr>\n\t\t\t\t\t\t\t\t<td class=\"fallback-text\" style=\"margin: 0; padding: 15px; background: #eeeeee; font-family: 'Roboto', Arial, Helvetica, sans-serif; font-size: 25px; line-height: 29px; text-align: center;\">\n\t\t\t\t\t\t\t\t\t<p style=\"color: #3F80C2; margin: 0; padding: 0; text-transform: uppercase;\">\n\t\t\t\t\t\t\t\t\t\tSERVIÇOS CONECTADOS\n\t\t\t\t\t\t\t\t\t</p>\n\t\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t</tr>\n\t\t\t\t\t\t</table>\n\t\t\t\t\t\t<!--  -->\n\t\t\t\t\t</td>\n\t\t\t\t</tr>\n\t\n\t\t\t\t<!-- ########################### BODY ###########################  -->\n\t\t\t\t<tr>\n\t\t\t\t\t<td>\n\t\t\t\t\t\t<table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"max-width: 600px; width: 100%; line-height: normal;\">\n\t\t\t\t\t\t\t<tr>\n\t\t\t\t\t\t\t\t<td class=\"fallback-text\" style=\"margin: 0; padding: 25px; color: #000000; font-family: 'Roboto', Arial, Helvetica, sans-serif; font-size: 16px; line-height: 23px; text-align: left;\">\n\t\t\t\t\t\t\t\t\t<!-- Name -->\n\t\t\t\t\t\t\t\t\t<p style=\"margin: 0; padding: 0; color: #000000;\">\n\t\t\t\t\t\t\t\t\t\t[$.Data.userProfile.firstName],\n\t\t\t\t\t\t\t\t\t</p>\n\t\t\t\t\t\t\t\t\t<img height=\"12\" style=\"margin: 0; padding: 0; border: none; display: block; max-width: 600px; width: 100%;\" src=\"https://myicnnct.deep.com/content/dam/cws/deep/email/spacer.gif\" alt=\"spacer\" />\n\t\t\t\t\t\t\t\t\t<!-- Body Text -->\n\t\t\t\t\t\t\t\t\t<p align=\"justify\" style=\"margin: 0; padding: 0; color: #000000;\">\n\t\t\t\t\t\t\t\t\t\tObrigado por assinar os Serviços Conectados para o seu [$.Data.vehicleProfile.name]. Seguem as informações da sua assinatura:\n\t\t\t\t\t\t\t\t\t<p align=\"justify\" style=\"margin: 0; padding: 0; color: #000000;\"> \n\t\t\t\t\t\t\t\t\tProduto: [$.Data.productName]\n\t\t\t\t\t\t\t\t\t<p align=\"justify\" style=\"margin: 0; padding: 0; color: #000000;\"> \n\t\t\t\t\t\t\t\t\tData da assinatura: [[formatDate|yyyy-MM-dd ss:mm:HH z@dd-MM-yyyy HH:mm:ss z|[$.Data.purchaseDate]]]\n\t\t\t\t\t\t\t\t\t<p align=\"justify\" style=\"margin: 0; padding: 0; color: #000000;\"> \n\t\t\t\t\t\t\t\t\tData de vencimento: [[formatDate|yyyy-MM-dd ss:mm:HH z|dd-MM-yyyy HH:mm:ss z|[$.Data.SubExpirationDate]]]\n\t\t\t\t\t\t\t\t\t<img height=\"12\" style=\"margin: 0; padding: 0; border: none; display: block; max-width: 600px; width: 100%;\" src=\"https://myicnnct.deep.com/content/dam/cws/deep/email/spacer.gif\" alt=\"spacer\" />\n\t\t\t\t\t\t\t\t\t<!-- Help Info -->\n\t\t\t\t\t\t\t\t\t<p align=\"justify\" style=\"margin: 0; padding: 0; color: #000000;\">\n\t\t\t\t\t\t\t\t\t\tPara informações adicionais, visite o website <a href=\"https://myicnnct.deep.com/br/pt/login\" >deep</a>  ou entre em contato com a Central de Serviços Conectados no 0800-007-7128.\n\t\t\t\t\t\t\t\t\t</p>\n\t\t\t\t\t\t\t\t\t<img height=\"25\" style=\"margin: 0; padding: 0; border: none; display: block; max-width: 600px; width: 100%;\" src=\"https://myicnnct.deep.com/content/dam/cws/deep/email/spacer.gif\" alt=\"spacer\" />\n\t\t\t\t\t\t\t\t\t<!--  -->\n\t\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t</tr>\n\t\t\t\t\t\t</table>\n\t\t\t\t\t\t<!--  -->\n\t\t\t\t\t</td>\n\t\t\t\t</tr>\n\t\n\t\t\t\t<!-- ########################### FOOTER ###########################  -->\n\t\t\t\t<tr>\n\t\t\t\t\t<td style=\"margin: 0; padding: 0;\">\n\t\t\t\t\t\t<!-- Brand Black/Grey -->\n\t\t\t\t\t\t \n\t\t\t\t\t\t<table width=\"600\" class=\"fallback-text\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background: #3e3e3e; color: #ffffff; max-width: 600px; width: 100%; font-family: 'Roboto', Arial, Helvetica, sans-serif; font-size: 10px; line-height: 14px; text-align: left; color: #ffffff;\">\n\t\t\t\t\t\t\t<tr>\n\t\t\t\t\t\t\t\t<td style=\"margin: 0; vertical-align: top;padding-left: 25px;padding-right: 25px;padding-top: 25px; vertical-align: top;\">\n\t\t\t\t\t\t\t\t\t<!-- Disclaimer -->\n\t\t\t\t\t\t\t\t\t<p align=\"justify\" style=\"margin: 0; padding: 0;\">\n\t\t\t\t\t\t\t\t\t\tOs Serviços Conectados estão disponíveis somente em veículos comprados no Brasil e equipados com o módulo de telemetria. Os Serviços Conectados somente poderão ser utilizados após serem ativados e em lugares com cobertura de rede de telefonia móvel celular. Os Serviços Conectados deverão ser ativados e cumprir os requisitos mínimos de assinatura. Ao utilizar os Serviços Conectados você aceita os Termos e Condições e a Política de Privacidade, que podem ser acessados através do link  <a href=\"https://myicnnct.deep.com/br/pt/login\" style=\"color: white;\" target=\"_blank\"><u>https://myicnnct.deep.com/br/pt/</u></a>.\n\t\t\t\t\t\t\t\t\t\t2021 Astellanti N.V.. Todos os direitos reservados. deep é uma marca registrada Astellanti N.V. Este é um e-mail automático, por favor, não responda.\n\t\t\t\t\t\t\t\t\t</p>\n\t\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t</tr>\n\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t<tr>\n\t\t\t\t\t\t\t\t<td style=\"margin: 0;vertical-align: top;padding-left: 25px;padding-right: 25px;padding-top: 25px; vertical-align: top;\">\n\t\t\t\t\t\t\t\t\t<!-- physical-address -->\n\t\t\t\t\t\t\t\t\t<p style=\"margin: 0; padding: 0;\">\n\t\t\t\t\t\t\t\t\t</p>\n\t\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t</tr>\n\t\t\t\t\t\t</table>\n\t\t\t\t\t</td>\n\t\t\t\t</tr>\n\t\n\t\t\t</table>\n\t\t\t\t\n\t\t\t<!--[if (gte mso 9)|(IE)]>\n\t\t\t</td>\n\t\t\t</tr>\n\t\t\t</table>\n\t\t\t<![endif]-->\n\t\t\t\t\n\t\t</td>\n\t</tr>\n</table>\n<!-- prevent Gmail on iOS font size manipulation -->\n<div style=\"display:none;white-space:nowrap;font:15px courier;line-height:0\">&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;</div>\n</body>\n</html>\n";
        assertEquals(1, notificationTemplateService.validateDateTransformerTemplate(html).size());
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    public void testInvalidDatePattern() {
        String html =
            "<!doctype html>\n<html>\n<head>\n\t<meta charset=\"utf-8\">\n\t<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n\t<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">    \n    \n\t<title> PQRS Transactional Emails </title>\n\t<link href=\"https://fonts.googleapis.com/css?family=Roboto\" rel=\"stylesheet\">\n\t<style type=\"text/css\"> <!-- a, a:visited {text-decoration: none;} --> </style>\n\t<!--[if (gte mso 9)|(IE)]>\n\t\t<style type=\"text/css\"> .fallback-text {font-family: Arial, sans-serif !important;} </style>\n\t<![endif]-->\n</head>\n\n<body style=\"margin: 0; padding: 0; background: #ffffff;\">\n<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\" style=\"background: #ffffff;\">\n\t<tr>\n\t\t<td align=\"center\" style=\"margin: 0; padding: 0; vertical-align: top;\">\n\t\t\t\n\t\t\t<!--[if (gte mso 9)|(IE)]>\n\t\t\t<table align=\"center\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"600\">\n\t\t\t<tr>\n\t\t\t<td align=\"center\" valign=\"top\" width=\"600\">\n\t\t\t<![endif]-->\t\t\t\n\t\t\t\n\t\t\t<table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"max-width: 600px; width: 100%; background: #ffffff;\">\n\t\t\t\t<!-- ########################### HEADER ###########################  -->\n\t\t\t\t<tr>\n\t\t\t\t\t<td>\t\t\t\t\t\t\n\t\t\t\t\t\t<!-- Pre-Header -->\n\t\t\t\t\t\t<table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"max-width: 600px; width: 100%; line-height: normal;\">\n\t\t\t\t\t\t\t<tr>\n\t\t\t\t\t\t\t\t<td class=\"fallback-text\" style=\"margin: 0; padding: 5px; font-family: 'Roboto', Arial, Helvetica, sans-serif; font-size: 10px; line-height: 13px; text-align: left;\">\n\t\t\t\t\t\t\t\t\t<p style=\"color: #000000; margin: 0; padding: 0;\">\n\t\t\t\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t\t\t</p>\n\t\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t</tr>\n\t\t\t\t\t\t</table>\n\t\t\t\t\t\t\n\t\t\t\t\t\t<!-- Vehicle Brand Logo / Connectivity Logo -->\n\t\t\t\t\t\t<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"max-width: 600px; width: 100%; line-height: normal; text-align: center;\">\n\t\t\t\t\t\t\t<tr>\n\t\t\t\t\t\t\t\t<td style=\"margin: 0; padding: 7px; text-align: right; vertical-align: middle;\">\n\t\t\t\t\t\t\t\t\t<img src=\"https://myicnnct.deep.com/content/dam/cws/deep/email/logo_deep.png\" height=\"60\" style=\"margin: 0; padding: 0; border: none;\" alt=\"Image-hdr_brand\">\n\t\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t</tr>\n\t\t\t\t\t\t</table>\n\t\t\t\t\t\t\n\t\t\t\t\t\t<!-- Headline Text -->\n\t\t\t\t\t\t<table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"max-width: 600px; width: 100%; line-height: normal;\">\n\t\t\t\t\t\t\t<tr>\n\t\t\t\t\t\t\t\t<td class=\"fallback-text\" style=\"margin: 0; padding: 15px; background: #eeeeee; font-family: 'Roboto', Arial, Helvetica, sans-serif; font-size: 25px; line-height: 29px; text-align: center;\">\n\t\t\t\t\t\t\t\t\t<p style=\"color: #3F80C2; margin: 0; padding: 0; text-transform: uppercase;\">\n\t\t\t\t\t\t\t\t\t\tSERVIÇOS CONECTADOS\n\t\t\t\t\t\t\t\t\t</p>\n\t\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t</tr>\n\t\t\t\t\t\t</table>\n\t\t\t\t\t\t<!--  -->\n\t\t\t\t\t</td>\n\t\t\t\t</tr>\n\t\n\t\t\t\t<!-- ########################### BODY ###########################  -->\n\t\t\t\t<tr>\n\t\t\t\t\t<td>\n\t\t\t\t\t\t<table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"max-width: 600px; width: 100%; line-height: normal;\">\n\t\t\t\t\t\t\t<tr>\n\t\t\t\t\t\t\t\t<td class=\"fallback-text\" style=\"margin: 0; padding: 25px; color: #000000; font-family: 'Roboto', Arial, Helvetica, sans-serif; font-size: 16px; line-height: 23px; text-align: left;\">\n\t\t\t\t\t\t\t\t\t<!-- Name -->\n\t\t\t\t\t\t\t\t\t<p style=\"margin: 0; padding: 0; color: #000000;\">\n\t\t\t\t\t\t\t\t\t\t[$.Data.userProfile.firstName],\n\t\t\t\t\t\t\t\t\t</p>\n\t\t\t\t\t\t\t\t\t<img height=\"12\" style=\"margin: 0; padding: 0; border: none; display: block; max-width: 600px; width: 100%;\" src=\"https://myicnnct.deep.com/content/dam/cws/deep/email/spacer.gif\" alt=\"spacer\" />\n\t\t\t\t\t\t\t\t\t<!-- Body Text -->\n\t\t\t\t\t\t\t\t\t<p align=\"justify\" style=\"margin: 0; padding: 0; color: #000000;\">\n\t\t\t\t\t\t\t\t\t\tObrigado por assinar os Serviços Conectados para o seu [$.Data.vehicleProfile.name]. Seguem as informações da sua assinatura:\n\t\t\t\t\t\t\t\t\t<p align=\"justify\" style=\"margin: 0; padding: 0; color: #000000;\"> \n\t\t\t\t\t\t\t\t\tProduto: [$.Data.productName]\n\t\t\t\t\t\t\t\t\t<p align=\"justify\" style=\"margin: 0; padding: 0; color: #000000;\"> \n\t\t\t\t\t\t\t\t\tData da assinatura: [[formatDate|yyyy-mm-dd ss:mm:HH z@dd-MM-yyyy HH:mm:ss z|[$.Data.purchaseDate]]]\n\t\t\t\t\t\t\t\t\t<p align=\"justify\" style=\"margin: 0; padding: 0; color: #000000;\"> \n\t\t\t\t\t\t\t\t\tData de vencimento: [[formatDate|jafnsdkfnsfnsdnfd z|dd-MM-yyyy HH:mm:ss z|[$.Data.SubExpirationDate]]]\n\t\t\t\t\t\t\t\t\t<img height=\"12\" style=\"margin: 0; padding: 0; border: none; display: block; max-width: 600px; width: 100%;\" src=\"https://myicnnct.deep.com/content/dam/cws/deep/email/spacer.gif\" alt=\"spacer\" />\n\t\t\t\t\t\t\t\t\t<!-- Help Info -->\n\t\t\t\t\t\t\t\t\t<p align=\"justify\" style=\"margin: 0; padding: 0; color: #000000;\">\n\t\t\t\t\t\t\t\t\t\tPara informações adicionais, visite o website <a href=\"https://myicnnct.deep.com/br/pt/login\" >deep</a>  ou entre em contato com a Central de Serviços Conectados no 0800-007-7128.\n\t\t\t\t\t\t\t\t\t</p>\n\t\t\t\t\t\t\t\t\t<img height=\"25\" style=\"margin: 0; padding: 0; border: none; display: block; max-width: 600px; width: 100%;\" src=\"https://myicnnct.deep.com/content/dam/cws/deep/email/spacer.gif\" alt=\"spacer\" />\n\t\t\t\t\t\t\t\t\t<!--  -->\n\t\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t</tr>\n\t\t\t\t\t\t</table>\n\t\t\t\t\t\t<!--  -->\n\t\t\t\t\t</td>\n\t\t\t\t</tr>\n\t\n\t\t\t\t<!-- ########################### FOOTER ###########################  -->\n\t\t\t\t<tr>\n\t\t\t\t\t<td style=\"margin: 0; padding: 0;\">\n\t\t\t\t\t\t<!-- Brand Black/Grey -->\n\t\t\t\t\t\t \n\t\t\t\t\t\t<table width=\"600\" class=\"fallback-text\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background: #3e3e3e; color: #ffffff; max-width: 600px; width: 100%; font-family: 'Roboto', Arial, Helvetica, sans-serif; font-size: 10px; line-height: 14px; text-align: left; color: #ffffff;\">\n\t\t\t\t\t\t\t<tr>\n\t\t\t\t\t\t\t\t<td style=\"margin: 0; vertical-align: top;padding-left: 25px;padding-right: 25px;padding-top: 25px; vertical-align: top;\">\n\t\t\t\t\t\t\t\t\t<!-- Disclaimer -->\n\t\t\t\t\t\t\t\t\t<p align=\"justify\" style=\"margin: 0; padding: 0;\">\n\t\t\t\t\t\t\t\t\t\tOs Serviços Conectados estão disponíveis somente em veículos comprados no Brasil e equipados com o módulo de telemetria. Os Serviços Conectados somente poderão ser utilizados após serem ativados e em lugares com cobertura de rede de telefonia móvel celular. Os Serviços Conectados deverão ser ativados e cumprir os requisitos mínimos de assinatura. Ao utilizar os Serviços Conectados você aceita os Termos e Condições e a Política de Privacidade, que podem ser acessados através do link  <a href=\"https://myicnnct.deep.com/br/pt/login\" style=\"color: white;\" target=\"_blank\"><u>https://myicnnct.deep.com/br/pt/</u></a>.\n\t\t\t\t\t\t\t\t\t\t2021 Astellanti N.V.. Todos os direitos reservados. deep é uma marca registrada Astellanti N.V. Este é um e-mail automático, por favor, não responda.\n\t\t\t\t\t\t\t\t\t</p>\n\t\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t</tr>\n\t\t\t\t\t\t\t\n\t\t\t\t\t\t\t<tr>\n\t\t\t\t\t\t\t\t<td style=\"margin: 0;vertical-align: top;padding-left: 25px;padding-right: 25px;padding-top: 25px; vertical-align: top;\">\n\t\t\t\t\t\t\t\t\t<!-- physical-address -->\n\t\t\t\t\t\t\t\t\t<p style=\"margin: 0; padding: 0;\">\n\t\t\t\t\t\t\t\t\t</p>\n\t\t\t\t\t\t\t\t</td>\n\t\t\t\t\t\t\t</tr>\n\t\t\t\t\t\t</table>\n\t\t\t\t\t</td>\n\t\t\t\t</tr>\n\t\n\t\t\t</table>\n\t\t\t\t\n\t\t\t<!--[if (gte mso 9)|(IE)]>\n\t\t\t</td>\n\t\t\t</tr>\n\t\t\t</table>\n\t\t\t<![endif]-->\n\t\t\t\t\n\t\t</td>\n\t</tr>\n</table>\n<!-- prevent Gmail on iOS font size manipulation -->\n<div style=\"display:none;white-space:nowrap;font:15px courier;line-height:0\">&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;</div>\n</body>\n</html>\n";
        assertEquals(2, notificationTemplateService.validateDateTransformerTemplate(html).size());
    }


    @Test
    public void importDynamicNotificationTemplateSuccess() throws Exception {
        Resource resource = new ClassPathResource("sample_template_valid_date_format.csv");
        InputStream input = resource.getInputStream();
        MultipartFile multipartFile = new MockMultipartFile("sample_template", input);
        List<DynamicNotificationTemplate> dynamicNotificationTemplateList = new ArrayList<>();
        DynamicNotificationTemplate dynamicNotificationTemplate = new DynamicNotificationTemplate();
        dynamicNotificationTemplate.setNotificationId(NOTIFICATION_ID_1);
        dynamicNotificationTemplateList.add(dynamicNotificationTemplate);
        assertNotNull(multipartFile);
        notificationTemplateService.importNotificationTemplate(multipartFile, false);
    }

    @Test(expected = InvalidInputFileException.class)
    public void importRichContentProperFileWithWarning() throws Exception {
        String filePath = DateFormatTransformerTest.IMPORT_RICH_CONTENT_RESOURCES_PATH + "InvalidFileDateFormat.zip";
        MockMultipartFile multipartFile =
            new MockMultipartFile("testFile", "", "", Files.readAllBytes(Paths.get(filePath)));
        richContentNotificationTemplateService.importNotificationTemplate(multipartFile, false);
    }

    @Test
    public void importRichContentProperFileSuccess() throws Exception {
        String filePath = DateFormatTransformerTest.IMPORT_RICH_CONTENT_RESOURCES_PATH + "ProperFileDateFormat.zip";
        MockMultipartFile multipartFile =
            new MockMultipartFile("testFile", "", "", Files.readAllBytes(Paths.get(filePath)));
        assertNotNull(multipartFile);
        richContentNotificationTemplateService.importNotificationTemplate(multipartFile, false);
    }

    @Test
    public void importRichContentProperFileAdditionalLookUpSuccess() throws Exception {
        String filePath = DateFormatTransformerTest.IMPORT_RICH_CONTENT_RESOURCES_PATH
            + "ProperDateFormatWithAdditionalLookUpProperty.zip";
        MockMultipartFile multipartFile =
            new MockMultipartFile("testFile", "", "", Files.readAllBytes(Paths.get(filePath)));
        assertNotNull(multipartFile);
        richContentNotificationTemplateService.importNotificationTemplate(multipartFile, false);
    }

}
