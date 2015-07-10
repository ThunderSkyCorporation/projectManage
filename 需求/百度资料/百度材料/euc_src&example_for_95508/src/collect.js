(function(){

	window.BDRP_euc = window.BDRP_euc || function(){
		
		// 默认的请求地址模板
		var LOG_URL_FORMAT = 'http://rpc.baidu.com/rpc/s/{appID}/{dataID}/{msg}/put.gif';

		var defaults = function(){
			var data = {
				appID: 0
				, dataID: 0
				, key: null
				, iv: null
				, logTemplate: LOG_URL_FORMAT
			};
			function genIVFromKeyStr(keyStr){
				var iv = [];
				for (var i=keyStr.length-1; i>=0; --i) {
					iv.push(keyStr.charAt(i));
				}
				return iv.join('');
			}
			return {
				get: function(name){
					return data[name];
				}
				, set: function(name, value){
					if (name == 'key') {
						data.key = CryptoJS.enc.Utf8.parse(value);
						data.iv = CryptoJS.enc.Utf8.parse(genIVFromKeyStr(value));
					} else {
						data[name] = value;
					}
				}
			}
		}();

		// 字符串格式化工具
		var format = function(){
	        function diggData(varStr, data) {
	            var keys = varStr.split('.'), temp = data;
	            for (var i=0,len=keys.length; i<len; i++) {
	                if (temp == undefined || temp == null) return undefined;
	                temp = temp[keys[i]];
	            }
	            return temp;
	        }
	        return function(str, data) {
	            return str.replace(/\{([^}]+)\}/g, function(match, $1){
	                var replace = diggData($1, data);
	                return (replace == undefined) ? match : replace;
	            });
	        }
	    }();

	    var encrypt = function(){
	    	return function(text){
	    		//调用CryptoJS库进行AES加密
				var encrypted = CryptoJS.AES.encrypt(text, defaults.get('key'), {iv: defaults.get('iv'), mode:CryptoJS.mode.CBC, padding:CryptoJS.pad.ZeroPadding});
	    		return encrypted.toString();
	    	}
	    }();

		function init() {
			// 设置aes加密参数
			defaults.set('key', 'GiveMeChocolate!');
			var self = document.getElementById('JS_BDRP-EUC');
			// 获取用户配置信息
			!!self.getAttribute('data-app-id') && defaults.set('appID', self.getAttribute('data-app-id'));
			!!self.getAttribute('data-data-id') && defaults.set('dataID', self.getAttribute('data-data-id'));
			!!self.getAttribute('data-log-template') && defaults.set('logTemplate', self.getAttribute('data-log-template'));
		}

		init();

		return {
			_pool: {}
			/**
			 * 发送请求
			 * @param url 请求的url
			 * @return undefined
			 */
			, send: function(url){
				// 生成时间戳，拼接到请求尾部，防止请求被浏览器缓存
				var stamp = '' + new Date().getTime() + ('000000'+(Math.floor(Math.random()*1000000))%1000000).slice(-6);
				var img = this._pool[stamp] = new Image(), that = this;
				img.onerror = img.onload = img.onreadystatechange = function(){
					if (!this.readyState || this.readyState == "loaded" || this.readyState == "complete") {
						this.onerror = this.onload = this.onreadystatechange = null;
						delete that._pool[stamp];
					}
				};
				img.src = (url.indexOf('?') == -1) ? url + '?' + stamp : url + '&' + stamp;
			}
			/**
			 * 信息发送
			 * @param str 需要发送的明文信息
			 * @param dataID 信息内容标识.
			 *	例如，如果所发送的信息为用户ID，则dataID为1
			 *	dataID定义请参照使用文档说明，如有新增信息内容需要提前与百度协商
			 * @return undefined
			 */
			, c: function(str, dataID) {
				// 对明文信息进行加密
				var msg = encrypt(str);
				// 拼接请求url
				var url = format(
					defaults.get('logTemplate'), 
					{
						appID: defaults.get('appID'), 
						dataID: dataID || defaults.get('dataID'), 
						msg: encodeURIComponent(msg)
					}
				);
				// 发送请求
				this.send(url);
			}
			, setDefault: function(key, value) {
				return defaults.set(key, value);
			}
		};
	}();

})();