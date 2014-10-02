var handler;

(function (document) {
	var slideString = function() {
		return window.location.hash.substring(1);
	};

	var setHash = function(index) {
		history.pushState(
			{index: index}, '',
			document.location.protocol + "//" +
				document.location.hostname +
					document.location.pathname + '#' + index);
	};

	var doAUI = function() {
		YUI().use(
			'aui-base', 'aui-event', 'anim', 'datatype-number',
			function(A) {
				var anim = new A.Anim({
					duration: 0.1,
					node: 'win',
					easing: 'easeBoth',
					to: {
						scroll: [0, 0]
					}
				});

				var scrollToSection = function(section, index) {
   					var y = section.getY() - 44;

					if (index == 0) {
						y = -40;
					}

					anim.set('to.scroll', [0, y]);
					setHash(index);
					anim.run();
				};

				var keyNavHandler = function (e) {
					if (!e.isKey('RIGHT') && !e.isKey('LEFT')) {
						return;
					}

					e.preventDefault();
					e.stopPropagation();

					var index = A.Number.parse(slideString()) || 0;
					var slides = A.all('#content > div');
					var max = slides.size();

					if(e.isKey('RIGHT')) {
						index++;

						if (index >= max) {
							index = 0;
						}
					}
					else if(e.isKey('LEFT')) {
						--index;

						if (index < 0) {
							index = (max - 1);
						}
					}

					var scrollDiv = slides.item(index);

					//console.log(scrollDiv);

					if (!scrollDiv) {
						return;
					}

					scrollToSection(scrollDiv, index);
				};

				if (!handler) {
					var body = A.one('body');

					handler = body.on('keyup', keyNavHandler);
				}
			}
		);
	};

	var auiScript = document.createElement('script');

	auiScript.type = 'text/javascript';
	auiScript.src = 'http://cdn.alloyui.com/2.0.0/aui/aui-min.js';
	auiScript.onload = function() {
		doAUI();
		document.addEventListener("DOMSubtreeModified", doAUI, false);
	};

	document.head.appendChild(auiScript);

}(document));