//
//  WINServiceManager.h
//  WiFi IoT Node
//
//  Created by Qxn on 15/7/8.
//  Copyright © 2015年 SeeedStudio. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "AFNetworking.h"



@protocol WINServiceDelegate <NSObject>

@optional
/**
 *  SignUp request succeed will call this method
 */
- (void)managerRequestSignUpSucceed;

/**
 *  SignUp request failed will call this method
 *
 *  @param msg The failure message
 */
- (void)managerRequestSignUpFailedWithMessage:(nullable NSString *)msg;

@end

@interface WINServiceManager : NSObject

@property(nullable, nonatomic,weak) id <WINServiceDelegate> delegate;
@property(nullable, nonatomic, strong) AFHTTPSessionManager *manager;

- (nullable instancetype)initWithDelegate:(nullable id)delegate;

/**
 *  Request sign up a new account. If success, the manager will call managerRequestSignUpSucceed delegate method,
 else the manager will call managerRequestRequestSignUpFailedWithMessage: delegate method
 *
 *  @param email    Sign up with the email address
 *  @param password Sign up with the password
 */
- (void)requestSignUpNewAccountWhitEmail:(nullable NSString *)email andPassword:(nullable NSString *)password;

/**
 *  cancel all operation Queue
 */
- (void)cancel;

@end
