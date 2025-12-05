package com.speedreader.trainer.di;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.speedreader.trainer.data.repository.UserRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class AppModule_ProvideUserRepositoryFactory implements Factory<UserRepository> {
  private final Provider<FirebaseFirestore> firestoreProvider;

  private final Provider<FirebaseAuth> firebaseAuthProvider;

  public AppModule_ProvideUserRepositoryFactory(Provider<FirebaseFirestore> firestoreProvider,
      Provider<FirebaseAuth> firebaseAuthProvider) {
    this.firestoreProvider = firestoreProvider;
    this.firebaseAuthProvider = firebaseAuthProvider;
  }

  @Override
  public UserRepository get() {
    return provideUserRepository(firestoreProvider.get(), firebaseAuthProvider.get());
  }

  public static AppModule_ProvideUserRepositoryFactory create(
      Provider<FirebaseFirestore> firestoreProvider, Provider<FirebaseAuth> firebaseAuthProvider) {
    return new AppModule_ProvideUserRepositoryFactory(firestoreProvider, firebaseAuthProvider);
  }

  public static UserRepository provideUserRepository(FirebaseFirestore firestore,
      FirebaseAuth firebaseAuth) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideUserRepository(firestore, firebaseAuth));
  }
}
