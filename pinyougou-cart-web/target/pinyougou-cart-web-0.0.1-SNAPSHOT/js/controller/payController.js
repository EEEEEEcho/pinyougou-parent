app.controller('payController', function($scope, payService) {
	$scope.createNative = function() {
		payService.createNative().success(function(response) {
			//显示订单号和金额
			$scope.total_fee = (response.total_fee / 100).toFixed(2);
			$scope.out_trade_no = response.out_trade_no;
			//生成二维码
			var qr = new QRious({
				element : document.getElementById('qrious'),
				size : 250,
				value : 'Pay success',
				level : 'H'
			});
		});
	}
//	$scope.queryPayStatus=function(out_trade_no){
//		payService.queryPayStatus(out_trade_no).success(
//				if(response.success){
//					location.href="paysuccess.html";
//				}else{					
//					location.href="payfail.html";								
//				}				
//			}
//		);
//	}
});