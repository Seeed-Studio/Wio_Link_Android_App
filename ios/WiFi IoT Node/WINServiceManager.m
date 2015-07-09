//
//  WINServiceManager.m
//  WiFi IoT Node
//
//  Created by Qxn on 15/7/8.
//  Copyright © 2015年 SeeedStudio. All rights reserved.
//

#import "WINServiceManager.h"


static NSString * const BaseURLString = @"https://iot.yuzhe.me/v1/";


@implementation WINServiceManager
- (nullable instancetype)initWithDelegate:(nullable id)delegate {
    self = [super init];
    self.delegate = delegate;
    return self;
}

- (AFHTTPSessionManager *)manager {
    if (_manager == nil) {
        NSURL *baseURL = [NSURL URLWithString:BaseURLString];

        _manager = [[AFHTTPSessionManager alloc] initWithBaseURL:baseURL];
    }
    return _manager;
}

- (void)requestSignUpNewAccountWhitEmail:(nullable NSString *)email andPassword:(nullable NSString *)password {
    
    NSDictionary *parameters = [NSDictionary dictionaryWithObjects:@[email, password] forKeys:@[@"email", @"password"]];
    
    self.manager.securityPolicy.allowInvalidCertificates = YES;
    //[manager.requestSerializer willChangeValueForKey:@"timeoutInterval"];
    self.manager.requestSerializer.timeoutInterval = 10.0f;
    //[manager.requestSerializer didChangeValueForKey:@"timeoutInterval"];
    
    NSLog(@"Manager securityPolicy = %@",self.manager.securityPolicy);
    self.manager.responseSerializer = [AFJSONResponseSerializer serializer];
    [self.manager POST:@"user/create" parameters:parameters success:^(NSURLSessionDataTask *task, id responseObject) {
        
        NSString *msg = [(NSDictionary *)responseObject objectForKey:@"msg"];

        if ([msg isEqualToString:@"User created"]) { //Creat new account succeed
            if ([self.delegate respondsToSelector:@selector(managerRequestSignUpSucceed)]) {
                [self.delegate managerRequestSignUpSucceed];
            }
        }
        if ([self.delegate respondsToSelector:@selector(managerRequestSignUpFailedWithMessage:)]) {
            [self.delegate managerRequestSignUpFailedWithMessage:msg];
        }

        NSLog(@"JSON: %@", responseObject);
        
    } failure:^(NSURLSessionDataTask *task, NSError *error) {
        if ([self.delegate respondsToSelector:@selector(managerRequestSignUpFailedWithMessage:)]) {
            [self.delegate managerRequestSignUpFailedWithMessage:error.helpAnchor];
        }

    }];
}

- (void)cancel {
    [self.manager.operationQueue cancelAllOperations];
}

@end
