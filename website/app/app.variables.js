app.constant('appName','CrowdPuller');
app.constant('appVersion','V1.0');
app.constant('compName','Bitwinger.com');
app.constant('apiPath','https://api.bitwinger.com/crowdpuller');
app.value('category',
	[
			{
			"parent" : "community",
			"child" : [
					{"id":7,"code":"activities"},
					{"id":8,"code":"childcare"},
					{"id":9,"code":"classes"},
					{"id":10,"code":"events"},
					{"id":11,"code":"general"},
					{"id":12,"code":"groups"},
					{"id":13,"code":"local news"},
					{"id":14,"code":"lost/found"},
					{"id":15,"code":"rideshare"},
					{"id":16,"code":"volunteers"},
					{"id":17,"code":"Others"}
				]
			},
			{
			"parent" : "Business Promotions",
			"child" : [
					{"id":18,"code":"Clothing"},
					{"id":19,"code":"Sports"},
					{"id":20,"code":"Food/Dining"},
					{"id":21,"code":"SuperMarkets"},
					{"id":22,"code":"Malls"},
					{"id":23,"code":"Jewelery"},
					{"id":24,"code":"General store"},
					{"id":25,"code":"Grocery store"},
					{"id":26,"code":"Hardware Store"},
					{"id":27,"code":"Pet Store"},
					{"id":28,"code":"Picture"},
					{"id":29,"code":"Shoe Store"},
					{"id":30,"code":"Toy Store"},
					{"id":31,"code":"Electronics"},
					{"id":32,"code":"Others"}
				]
			},
			{
			"parent" : "services",
			"child" : [
				{"id":33,"code":"automotive"},
				{"id":34,"code":"beauty"},
				{"id":35,"code":"computer"},
				{"id":36,"code":"creative"},
				{"id":37,"code":"cycle"},
				{"id":38,"code":"event"},
				{"id":39,"code":"farm+garden"},
				{"id":40,"code":"financial"},
				{"id":41,"code":"household"},
				{"id":42,"code":"labor/movers"},
				{"id":43,"code":"legal"},
				{"id":44,"code":"lessons"},
				{"id":45,"code":"Pets"},
				{"id":46,"code":"Classes"},
				{"id":47,"code":"real estate"},
				{"id":48,"code":"Healthcare"},
				{"id":49,"code":"travel/vacation"},
				{"id":50,"code":"Others"}
				]
			},
			{
			"parent" : "housing",
			"child" : [
				{"id":51,"code":"apts / housing"},
				{"id":52,"code":"housing wanted"},
				{"id":53,"code":"office / commercial"},
				{"id":54,"code":"parking / storage"},
				{"id":55,"code":"real estate for sale"},
				{"id":56,"code":"rooms / shared"},
				{"id":57,"code":"rooms wanted"},
				{"id":58,"code":"sublets / temporary"},
				{"id":59,"code":"vacation rentals"},
				{"id":60,"code":"PG"},
				{"id":61,"code":"Others"}
				]
			},
			{
			"parent" : "for sale",
			"child" : [
					{"id":62,"code":"antiques"},
					{"id":63,"code":"appliances"},
					{"id":64,"code":"arts+crafts"},
					{"id":65,"code":"atv/utv/sno"},
					{"id":66,"code":"auto parts"},
					{"id":67,"code":"bikes"},
					{"id":68,"code":"books"},
					{"id":69,"code":"business"},
					{"id":70,"code":"cars+trucks"},
					{"id":71,"code":"cds/dvd/vhs"},
					{"id":72,"code":"cell phones"},
					{"id":73,"code":"clothes+acc"},
					{"id":74,"code":"collectibles"},
					{"id":75,"code":"computers"},
					{"id":76,"code":"electronics"},
					{"id":77,"code":"farm+garden"},
					{"id":78,"code":"free"},
					{"id":79,"code":"furniture"},
					{"id":80,"code":"garage sale"},
					{"id":81,"code":"general"},
					{"id":82,"code":"heavy equip"},
					{"id":83,"code":"household"},
					{"id":84,"code":"jewelry"},
					{"id":85,"code":"materials"},
					{"id":86,"code":"motorcycles"},
					{"id":87,"code":"music instr"},
					{"id":88,"code":"photo+video"},
					{"id":89,"code":"sporting"},
					{"id":90,"code":"tickets"},
					{"id":91,"code":"tools"},
					{"id":92,"code":"toys+games"},
					{"id":93,"code":"video gaming"},
					{"id":94,"code":"wanted"},
					{"id":95,"code":"Others"}
				]
			},
			{
			"parent" : "jobs",
			"child" : [
				{"id":96,"code":"accounting+finance"},
				{"id":97,"code":"admin / office"},
				{"id":98,"code":"arch / engineering"},
				{"id":99,"code":"art / media / design"},
				{"id":100,"code":"biotech / science"},
				{"id":101,"code":"business / mgmt"},
				{"id":102,"code":"customer service"},
				{"id":103,"code":"education"},
				{"id":104,"code":"food / bev / hosp"},
				{"id":105,"code":"general labor"},
				{"id":106,"code":"government"},
				{"id":107,"code":"human resources"},
				{"id":108,"code":"internet engineers"},
				{"id":109,"code":"legal / paralegal"},
				{"id":110,"code":"manufacturing"},
				{"id":111,"code":"marketing / pr / ad"},
				{"id":112,"code":"medical / health"},
				{"id":113,"code":"nonprofit sector"},
				{"id":114,"code":"real estate"},
				{"id":115,"code":"retail / wholesale"},
				{"id":116,"code":"sales / biz dev"},
				{"id":117,"code":"salon / spa / fitness"},
				{"id":118,"code":"security"},
				{"id":119,"code":"skilled trade / craft"},
				{"id":120,"code":"software / qa / dba"},
				{"id":121,"code":"systems / network"},
				{"id":122,"code":"technical support"},
				{"id":123,"code":"transport"},
				{"id":124,"code":"tv / film / video"},
				{"id":125,"code":"web / info design"},
				{"id":126,"code":"writing / editing"},
				{"id":127,"code":"Others"}
				]
			}
		]
);
app.value('categoryflat',[]);
app.value('appVars', {
	masters : {
			flagReasons: [
					{"Id":"1","dscrpt":"false, misleading, deceptive, or fraudulent content"},
					{"Id":"2","dscrpt":"offensive, obscene, defamatory, threatening, or malicious content"},
					{"Id":"3","dscrpt":"anyones personal, identifying, confidential or proprietary information"},
					{"Id":"4","dscrpt":"child pornography; bestiality; offers or solicitation of illegal prostitution"},
					{"Id":"5","dscrpt":"spam; miscategorized, overposted, cross-posted, or nonlocal content"},
					{"Id":"6","dscrpt":"Selling stolen property, property with serial number removed\/altered, burglary tools, etc"},
					{"Id":"7","dscrpt":"Selling ID cards, licenses, police insignia, government documents, birth certificates, etc"},
					{"Id":"8","dscrpt":"Selling counterfeit, replica, or pirated items;"},
					{"Id":"9","dscrpt":"Selling lottery or raffle tickets, gambling items"},
					{"Id":"10","dscrpt":"affiliate marketing; network, or multi-level marketing; pyramid schemes"},
					{"Id":"11","dscrpt":"Selling ivory; endangered, imperiled and\/or protected species and any parts thereof"},
					{"Id":"12","dscrpt":"Selling alcohol or tobacco;"},
					{"Id":"13","dscrpt":"Selling prescription drugs, controlled substances and related items"},
					{"Id":"14","dscrpt":"Selling weapons; firearms\/guns; etc"},
					{"Id":"15","dscrpt":"Selling ammunition, gunpowder, explosives"},
					{"Id":"16","dscrpt":"Selling hazardous materials; body parts\/fluids;"},
					{"Id":"17","dscrpt":"any good, service, or content that violates the law or legal rights of others"}
				],
			responseType: [
					{"Id":"0","name":"None"},
					{"Id":"1","name":"Yes\/No\/Don't Know"},
					{"Id":"2","name":"Ratings (1-5 *)"},
					{"Id":"3","name":"Like\/Dislike\/Neutral"}
				],
			preferences: {
					"countryCode":"ALL",
					"newMemFreeCredit":"3",
					"refCreditsEarnPerInv":"0.25",
					"autoBanFlagCount":"10",
					"banPostLimitForAutoBlockMember":"5",
					"daysToExpirePost":"30",
					"minCovAreaInKM":"0.50",
					"maxCovAreaInKM":"25.00"
				},
			loaded:true
		},
	user : {
			sessionId:'',
			memberId:'',
			userName:'',
			fbToken:'',
			gpToken:'',
			address:'',
			isNewSignUp : false
		},
	fbAppId : _fbAppId,
	gpClientId : _gpClientId,
	fbSDKLoaded: false,
	gpSDKLoaded: false,
	fbSDKLoadedHndlr: null,
	gpSDKLoadedHndlr: null
});
